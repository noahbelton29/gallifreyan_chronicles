package uk.nbforge.gallifreyan_chronicles.tardis.mind.mood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisAlarmPayload;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.CrashSmoke;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;

/**
 * Alarms with flashing lights and cloister bell sounds.
 * The alarm runs while the cloister bell control is on, so anything that wants the alarm flips that control,
 * and switching the control off silences it.
 * An alarm started by something else turns the control back off by itself after a set number of flashes,
 * when the TARDIS calms down, or when a crashed TARDIS is repaired.
 */
public final class TardisAlarm {
    // Length of each flash and the gap between repeated sounds, in ticks
    public static final int FLASH_TICKS = 30;

    // Stress below this stops a stress alarm
    private static final float CALM_STRESS = 30f;

    // How many times the bell plays for a loud alarm
    private static final int LOUD_LAYERS = 3;

    private static final float INSIDE_VOLUME = 1.0F;
    private static final float OUTSIDE_VOLUME = 0.6F;

    // How far from the exterior the bell can be heard
    private static final double LEAK_RANGE = 32.0;

    /**
     * Why the alarm is ringing, which decides when it stops on its own. Later ones win over earlier ones.
     */
    private enum Mode {
        // A set number of flashes
        TIMED,
        // Until the TARDIS calms down
        UNTIL_CALM,
        // Until the TARDIS is repaired
        UNTIL_REPAIRED,
        // Turned on by hand, until it is turned off by hand
        MANUAL
    }

    private static final class Alarm {
        Mode mode;
        int flashesLeft;
        boolean loud;

        Alarm(Mode mode, int flashes, boolean loud) {
            this.mode = mode;
            this.flashesLeft = flashes;
            this.loud = loud;
        }

        void merge(Mode newMode, int flashes, boolean newLoud) {
            if (newMode == Mode.TIMED && mode == Mode.TIMED) {
                flashesLeft = Math.max(flashesLeft, flashes);
                loud |= newLoud;
            } else if (newMode.ordinal() > mode.ordinal()) {
                mode = newMode;
                loud = false;
            }
        }

        boolean finished(Tardis tardis) {
            return switch (mode) {
                case TIMED -> flashesLeft <= 0;
                case UNTIL_CALM -> TardisMindSystem.stress(tardis) < CALM_STRESS;
                case UNTIL_REPAIRED -> !tardis.data().isCrashed(tardis.id());
                case MANUAL -> false;
            };
        }
    }

    private record Request(Mode mode, int flashes, boolean loud) {}

    // Ringing alarms
    private static final Map<UUID, Alarm> ACTIVE = ServerState.track(new HashMap<>());

    // Why the control is about to be switched on, read when it is
    private static final Map<UUID, Request> PENDING = ServerState.track(new HashMap<>());

    private TardisAlarm() {}

    public static void reset() {
        ACTIVE.clear();
        PENDING.clear();
    }

    public static boolean isRinging(Tardis tardis) {
        return ACTIVE.containsKey(tardis.id());
    }

    public static void untilRepaired(Tardis tardis) {
        request(tardis, Mode.UNTIL_REPAIRED, 0, false);
    }

    /**
     * Runs until the stress falls below the calm level.
     */
    public static void untilCalm(Tardis tardis, ServerLevel level, BlockPos bellPos) {
        request(tardis, Mode.UNTIL_CALM, 0, false);
    }

    public static void flash(ServerLevel level, BlockPos bellPos, int times) {
        timed(level, times, false);
    }

    public static void flashLoud(ServerLevel level, int times) {
        timed(level, times, true);
    }

    private static void timed(ServerLevel level, int times, boolean loud) {
        if (times <= 0) return;
        Tardis tardis = TardisApi.ofLevel(level).orElse(null);
        if (tardis == null) {
            // Not inside a TARDIS, there is no control to flip so just flash
            TardisAlarmPayload.broadcast(level, times);
            for (int i = 0; i < times; i++) {
                GCSchedulerUtil.runLater(level.getServer(), i * FLASH_TICKS, () -> ring(level, loud));
            }
            return;
        }
        request(tardis, Mode.TIMED, times, loud);
    }

    /**
     * So alarms start again after the server restarts.
     * A crashed TARDIS rings until repaired; any other alarm was lost with the restart so the control goes back off.
     */
    public static void resumeCrashAlarms(MinecraftServer server) {
        for (Tardis tardis : TardisApi.all(server)) {
            if (tardis.data().isCrashed(tardis.id())) {
                untilRepaired(tardis);
                CrashSmoke.start(tardis);
            } else {
                tardis.syncControl(GCTardisControls.CLOISTER_BELL_CONTROL, false);
            }
        }
    }

    /**
     * Something wants the alarm, so the cloister bell control is switched on and that starts it.
     */
    private static void request(Tardis tardis, Mode mode, int flashes, boolean loud) {
        Alarm running = ACTIVE.get(tardis.id());
        if (running != null) {
            running.merge(mode, flashes, loud);
            return;
        }

        PENDING.put(tardis.id(), new Request(mode, flashes, loud));
        tardis.setControlState(GCTardisControls.CLOISTER_BELL_CONTROL, true);

        // No console loaded, or the control was already on, so it did not start the alarm itself
        Request left = PENDING.remove(tardis.id());
        if (left != null) begin(tardis, left);
    }

    /**
     * The cloister bell control was switched on.
     */
    public static void controlOn(Tardis tardis) {
        Request request = PENDING.remove(tardis.id());
        begin(tardis, request == null ? new Request(Mode.MANUAL, 0, false) : request);
    }

    /**
     * The cloister bell control was switched off, which silences any alarm.
     */
    public static void controlOff(Tardis tardis) {
        PENDING.remove(tardis.id());
        ACTIVE.remove(tardis.id());
        tardis.interiorLevel().ifPresent(level -> TardisAlarmPayload.broadcast(level, TardisAlarmPayload.STOP));
    }

    private static void begin(Tardis tardis, Request request) {
        Alarm running = ACTIVE.get(tardis.id());
        if (running != null) {
            running.merge(request.mode(), request.flashes(), request.loud());
            return;
        }
        Alarm alarm = new Alarm(request.mode(), request.flashes(), request.loud());
        ACTIVE.put(tardis.id(), alarm);
        tick(tardis, alarm);
    }

    private static void tick(Tardis tardis, Alarm alarm) {
        // Switched off, or replaced by a newer alarm
        if (ACTIVE.get(tardis.id()) != alarm) return;

        if (!tardis.exists()) {
            ACTIVE.remove(tardis.id());
            return;
        }

        if (alarm.finished(tardis)) {
            // Turning the control off stops the alarm, and if no console is loaded stop it here
            tardis.setControlState(GCTardisControls.CLOISTER_BELL_CONTROL, false);
            if (ACTIVE.get(tardis.id()) == alarm) {
                tardis.syncControl(GCTardisControls.CLOISTER_BELL_CONTROL, false);
                controlOff(tardis);
            }
            return;
        }

        // Keeps a console that loads in later showing the control as on
        tardis.syncControl(GCTardisControls.CLOISTER_BELL_CONTROL, true);

        tardis.interiorLevel().ifPresent(level -> {
            TardisAlarmPayload.broadcast(level, TardisAlarmPayload.FOREVER);
            ring(level, alarm.loud);
        });
        if (alarm.mode == Mode.TIMED) alarm.flashesLeft--;

        GCSchedulerUtil.runLater(tardis.server(), FLASH_TICKS, () -> tick(tardis, alarm));
    }

    private static void ring(ServerLevel level, boolean loud) {
        int layers = loud ? LOUD_LAYERS : 1;
        for (int i = 0; i < layers; i++) {
            bell(level);
        }
    }

    /**
     * Full volume inside and quieter outside.
     */
    private static void bell(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            ringAt(player, INSIDE_VOLUME);
        }
        for (ServerPlayer player : playersOutside(level)) {
            ringAt(player, OUTSIDE_VOLUME);
        }
    }

    private static void ringAt(ServerPlayer player, float volume) {
        Holder<SoundEvent> sound = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GCSounds.TARDIS_CLOISTER_BELL);
        player.connection.send(new ClientboundSoundPacket(
                sound,
                SoundSource.BLOCKS,
                player.getX(),
                player.getEyeY(),
                player.getZ(),
                volume,
                1.0F,
                player.getRandom().nextLong()));
    }

    /**
     * The distance is measured from the exterior block position.
     */
    public static List<ServerPlayer> playersOutside(ServerLevel interior) {
        Tardis tardis = TardisApi.ofLevel(interior).orElse(null);
        if (tardis == null) return List.of();
        TardisExteriorBlockEntity exterior = tardis.exterior().orElse(null);
        if (exterior == null || !(exterior.getLevel() instanceof ServerLevel outside)) return List.of();
        if (exterior.isFullyInvisible()) return List.of();
        Vec3 doors = Vec3.atCenterOf(exterior.getBlockPos());
        List<ServerPlayer> near = new ArrayList<>();
        for (ServerPlayer player : outside.players()) {
            if (player.distanceToSqr(doors) <= LEAK_RANGE * LEAK_RANGE) near.add(player);
        }
        return near;
    }
}
