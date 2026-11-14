package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood;

import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.CrashSmoke;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;

import net.minecraft.server.MinecraftServer;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;

import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisAlarmPayload;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles TARDIS alarms, including flashing lights and cloister bell sounds.
 * <p>
 * Alarms can run for a fixed number of flashes, continue until the TARDIS
 * calms down, or remain active until a crashed TARDIS is repaired.
 */
public final class TardisAlarm {
    /**
     * Duration of each alarm flash and interval between repeated sounds, in ticks.
     */
    public static final int FLASH_TICKS = 30;

    /**
     * Stress level below which a continuous stress alarm stops.
     */
    private static final float CALM_STRESS = 30f;

    /**
     * Number of times the bell is played for a loud alarm.
     */
    private static final int LOUD_LAYERS = 3;

    private static final float INSIDE_VOLUME = 1.0F;
    private static final float OUTSIDE_VOLUME = 0.6F;

    /**
     * Maximum distance from the TARDIS exterior at which the bell can be heard.
     */
    private static final double LEAK_RANGE = 32.0;

    /**
     * TARDISes currently running a stress-based alarm.
     */
    private static final Set<UUID> RUNNING = new HashSet<>();

    /**
     * TARDISes whose crash alarms continue until they are repaired.
     */
    private static final Set<UUID> UNTIL_REPAIRED = new HashSet<>();

    private TardisAlarm() {
    }

    public static void reset() {
        RUNNING.clear();
        UNTIL_REPAIRED.clear();
    }

    /**
     * Starts an alarm that continues until the TARDIS is repaired.
     *
     * @param tardis the crashed TARDIS
     */
    public static void untilRepaired(Tardis tardis) {
        if (UNTIL_REPAIRED.add(tardis.id())) {
            repairTick(tardis);
        }
    }

    /**
     * Restarts crash alarms for TARDISes that are still crashed.
     * <p>
     * This allows alarms to resume after the server starts up again.
     *
     * @param server the server containing the TARDISes
     */
    public static void resumeCrashAlarms(MinecraftServer server) {
        for (Tardis tardis : TardisApi.all(server)) {
            if (tardis.data().isCrashed(tardis.id())) {
                untilRepaired(tardis);
                CrashSmoke.start(tardis);
            }
        }
    }

    /**
     * Repeatedly checks whether a crashed TARDIS has been repaired.
     * <p>
     * While it remains crashed, the alarm continues to flash and ring.
     * Once repaired or removed, the alarm is stopped.
     *
     * @param tardis the TARDIS being monitored
     */
    private static void repairTick(Tardis tardis) {
        if (!UNTIL_REPAIRED.contains(tardis.id())) return;
        ServerLevel level = tardis.interiorLevel().orElse(null);

        boolean repaired = !tardis.exists() || !tardis.data().isCrashed(tardis.id());
        if (repaired) {
            UNTIL_REPAIRED.remove(tardis.id());
            if (level != null) TardisAlarmPayload.broadcast(level, TardisAlarmPayload.STOP);
            return;
        }

        if (level != null) {
            TardisAlarmPayload.broadcast(level, TardisAlarmPayload.FOREVER);
            bell(level);
        }
        GCScheduler.runLater(tardis.server(), FLASH_TICKS, () -> repairTick(tardis));
    }

    public static void flash(ServerLevel level, BlockPos bellPos, int times) {
        flashWithBell(level, times, () -> bell(level));
    }

    public static void flashLoud(ServerLevel level, int times) {
        flashWithBell(level, times, () -> loudBell(level));
    }

    /**
     * Broadcasts a fixed-duration alarm and schedules its bell sounds.
     * <p>
     * Each sound is scheduled one flash interval apart.
     *
     * @param level the level where the alarm is played
     * @param times the number of flashes and bell sounds
     * @param ring  the sound action to perform for each interval
     */
    private static void flashWithBell(ServerLevel level, int times, Runnable ring) {
        TardisAlarmPayload.broadcast(level, times);
        for (int i = 0; i < times; i++) {
            GCScheduler.runLater(level.getServer(), i * FLASH_TICKS, ring);
        }
    }

    /**
     * Starts an alarm that continues until the TARDIS's stress falls below
     * the calm threshold.
     *
     * @param tardis  the TARDIS whose stress is being monitored
     * @param level   the level where the alarm is played
     * @param bellPos the bell position
     */
    public static void untilCalm(Tardis tardis, ServerLevel level, BlockPos bellPos) {
        if (!RUNNING.add(tardis.id())) return;
        TardisAlarmPayload.broadcast(level, TardisAlarmPayload.FOREVER);
        tick(tardis, level, bellPos);
    }

    /**
     * Checks whether a stress-based alarm should continue.
     * <p>
     * Stops the alarm when the TARDIS is removed or its stress drops below
     * {@link #CALM_STRESS}.
     *
     * @param tardis  the TARDIS being monitored
     * @param level   the level where the alarm is played
     * @param bellPos the bell position
     */
    private static void tick(Tardis tardis, ServerLevel level, BlockPos bellPos) {
        if (!RUNNING.contains(tardis.id())) return;

        boolean calmedDown = !tardis.exists() || TardisMindSystem.stress(tardis) < CALM_STRESS;
        if (calmedDown) {
            RUNNING.remove(tardis.id());
            TardisAlarmPayload.broadcast(level, TardisAlarmPayload.STOP);
            return;
        }

        TardisAlarmPayload.broadcast(level, TardisAlarmPayload.FOREVER);
        bell(level);
        GCScheduler.runLater(level.getServer(), FLASH_TICKS, () -> tick(tardis, level, bellPos));
    }

    private static void loudBell(ServerLevel level) {
        for (int i = 0; i < LOUD_LAYERS; i++) {
            bell(level);
        }
    }

    /**
     * Plays the cloister bell for players inside the TARDIS and nearby players
     * outside its exterior.
     * <p>
     * The sound is played at full volume inside and reduced volume outside.
     *
     * @param level the TARDIS interior level
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
        player.connection.send(new ClientboundSoundPacket(sound, SoundSource.BLOCKS,
                player.getX(), player.getEyeY(), player.getZ(), volume, 1.0F, player.getRandom().nextLong()));
    }

    /**
     * Finds players outside the TARDIS who are close enough to hear its bell.
     * <p>
     * Uses the exterior's block position as the centre of the distance check.
     *
     * @param interior the TARDIS interior level
     * @return nearby players in the exterior level, or an empty list if the
     * TARDIS or its loaded exterior cannot be found
     */
    public static List<ServerPlayer> playersOutside(ServerLevel interior) {
        Tardis tardis = TardisApi.ofLevel(interior).orElse(null);
        if (tardis == null) return List.of();
        TardisExteriorBlockEntity exterior = tardis.exterior().orElse(null);
        if (exterior == null || !(exterior.getLevel() instanceof ServerLevel outside)) return List.of();
        Vec3 doors = Vec3.atCenterOf(exterior.getBlockPos());
        List<ServerPlayer> near = new ArrayList<>();
        for (ServerPlayer player : outside.players()) {
            if (player.distanceToSqr(doors) <= LEAK_RANGE * LEAK_RANGE) near.add(player);
        }
        return near;
    }
}