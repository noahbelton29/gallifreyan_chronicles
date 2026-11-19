package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeTiming;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTurbulence;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisMoods;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal.TardisRefusals;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public final class TardisFlight {
    private static final float VWORP_VOLUME = 0.35f;
    private static final int TAKEOFF_SOUND_DELAY_TICKS = 0;
    private static final int TAKEOFF_SOUND_LENGTH_TICKS = FlightTimings.TAKEOFF_TICKS;
    private static final int QUEUED_TAKEOFF_POLL_TICKS = 2;
    private static final int LAND_FADE_DELAY_TICKS = 130;
    private static final int LAND_SOUND_LENGTH_TICKS = 393;
    private static final int ABORT_FADE_IN_TICKS = 30;
    private static final float EMERGENCY_DISTANCE = 0.75f;

    private static final Map<UUID, Integer> QUEUED_TAKEOFFS = ServerState.track(new HashMap<>());
    private static int queueGeneration = 0;

    private TardisFlight() {}

    public static void resetState() {
        QUEUED_TAKEOFFS.clear();
    }

    /**
     * false if the tardis refused or the lever got put back
     */
    public static boolean leverPulled(
            TardisConsoleBlockEntity console,
            ServerLevel level,
            String controlId,
            BlockPos soundPos,
            Vec3 leverPos,
            @Nullable Player pilot) {
        boolean takingOff = !console.getControlState(controlId);
        Tardis tardis = Tardis.of(console).orElse(null);

        if (refuseToggle(level, soundPos, leverPos, console, tardis, takingOff, pilot)) return false;

        if (takingOff) {
            beginTakeoffEffects(level, console, leverPos);
        } else {
            land(level, console, tardis);
        }
        return true;
    }

    /**
     * false if it refused
     */
    public static boolean pullThrottle(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) return false;
        Tardis tardis = Tardis.of(console).orElse(null);
        BlockPos soundPos = console.getBlockPos();
        boolean takingOff = !console.getControlState(GCTardisControls.SPACE_TIME_THROTTLE);

        if (refuseToggle(level, soundPos, Vec3.atCenterOf(soundPos), console, tardis, takingOff, null)) return false;

        ConsoleSounds.at(level, soundPos, GCSounds.SLIDER, takingOff ? 0.9f : 1.0f);
        if (takingOff) {
            beginTakeoffEffects(level, console, Vec3.atCenterOf(soundPos));
        } else {
            land(level, console, tardis);
        }
        console.triggerControl(GCTardisControls.SPACE_TIME_THROTTLE, takingOff, null);
        return true;
    }

    public static void throttleChanged(TardisConsoleBlockEntity console, boolean activated) {
        GCLog.TARDIS.debug("Flight lever state changed to: {}", activated);

        if (console.getLevel() instanceof ServerLevel serverLevel) {
            if (activated) announceDeparture(serverLevel);
            TardisExteriorBlockEntity exterior =
                    Tardis.of(console).flatMap(Tardis::exterior).orElse(null);
            if (exterior != null) {
                if (activated) {
                    exterior.travel().cancelLanding();
                    exterior.travel().setInFlight(true);
                    exterior.travel()
                            .setFlightState(
                                    TardisFlightState.TAKING_OFF,
                                    TAKEOFF_SOUND_DELAY_TICKS + TAKEOFF_SOUND_LENGTH_TICKS);
                } else {
                    exterior.travel().scheduleLanding(LAND_FADE_DELAY_TICKS);
                    exterior.travel()
                            .setFlightState(
                                    TardisFlightState.LANDING,
                                    LAND_FADE_DELAY_TICKS
                                            + exterior.travel()
                                                    .getFadeType()
                                                    .timing()
                                                    .fadeInTicks());
                }
            }
        }

        if (activated) {
            for (TardisConsoleBlockEntity each : console.linkedConsoles()) {
                each.triggerRotorAnimation("rotor_flight");
            }
        } else {
            scheduleLandingFinish(console);
        }
    }

    public static void crashLand(TardisConsoleBlockEntity console) {
        crashLand(console, "crash_landing");
    }

    /**
     * message is the lang key shown as it falls out of the vortex
     */
    public static void crashLand(TardisConsoleBlockEntity console, String message) {
        if (!(console.getLevel() instanceof ServerLevel level)) return;
        Tardis tardis = Tardis.of(console).orElse(null);
        TardisExteriorBlockEntity exterior =
                tardis == null ? null : tardis.exterior().orElse(null);
        if (exterior == null || exterior.travel().getFlightState() != TardisFlightState.IN_VORTEX) return;

        int ticks =
                LAND_FADE_DELAY_TICKS + exterior.travel().getFadeType().timing().fadeInTicks();
        MessageUtil.overlayNearby(level, Vec3.atCenterOf(console.getBlockPos()), ChatFormatting.DARK_RED, message);
        TardisCrash.run(tardis, level, console.getBlockPos(), ticks);

        tardis.data().setLandingTarget(TardisTravel.crashTarget(tardis));
        ConsoleSounds.at(level, console.getBlockPos(), GCSounds.SLIDER, 1.0f);
        land(level, console, tardis);
        console.triggerControl(GCTardisControls.SPACE_TIME_THROTTLE, false, null);
    }

    /**
     * stressed tardis dropping out of the vortex on its own. rough but not a crash, no damage and it gets most of the
     * way there
     */
    public static boolean emergencyLand(TardisConsoleBlockEntity console, String message) {
        if (!(console.getLevel() instanceof ServerLevel level)) return false;
        Tardis tardis = Tardis.of(console).orElse(null);
        TardisExteriorBlockEntity exterior =
                tardis == null ? null : tardis.exterior().orElse(null);
        if (exterior == null || exterior.travel().getFlightState() != TardisFlightState.IN_VORTEX) return false;

        int ticks =
                LAND_FADE_DELAY_TICKS + exterior.travel().getFadeType().timing().fadeInTicks();
        MessageUtil.overlayNearby(level, Vec3.atCenterOf(console.getBlockPos()), ChatFormatting.GOLD, message);
        TardisCrash.emergency(tardis, level, console.getBlockPos(), ticks);

        tardis.data().setLandingTarget(TardisTravel.partWayTarget(tardis, EMERGENCY_DISTANCE));
        ConsoleSounds.at(level, console.getBlockPos(), GCSounds.SLIDER, 1.0f);
        land(level, console, tardis);
        console.triggerControl(GCTardisControls.SPACE_TIME_THROTTLE, false, null);
        return true;
    }

    /**
     * false if its not taking off
     */
    public static boolean abortTakeoff(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) return false;
        Optional<Tardis> tardis = Tardis.of(console);
        TardisExteriorBlockEntity exterior = tardis.flatMap(Tardis::exterior).orElse(null);
        if (exterior == null || exterior.travel().getFlightState() != TardisFlightState.TAKING_OFF) return false;

        QUEUED_TAKEOFFS.remove(tardis.get().id());
        TardisMoods.onTakeoffForcedStop(tardis.get());
        List<TardisConsoleBlockEntity> consoles = console.linkedConsoles();

        Set<ServerLevel> levels = new LinkedHashSet<>();
        levels.add(level);
        for (TardisConsoleBlockEntity each : consoles) {
            if (each.getLevel() instanceof ServerLevel eachLevel) levels.add(eachLevel);
        }
        if (exterior.getLevel() instanceof ServerLevel exteriorLevel) levels.add(exteriorLevel);
        for (ServerLevel each : levels) {
            stopSound(each, GCSounds.TARDIS_TAKEOFF);
            stopSound(each, GCSounds.TARDIS_VWORP);
        }

        ConsoleSounds.everywhere(console, GCSounds.TARDIS_STOP_DEMAT);
        exterior.travel().abortTakeoff(ABORT_FADE_IN_TICKS);

        for (TardisConsoleBlockEntity each : consoles) {
            each.setControlActivated(GCTardisControls.SPACE_TIME_THROTTLE, false);
            each.triggerRotorAnimation("rotor_idle");
        }

        tardis.get().setControlState(GCTardisControls.NAVIGATIONAL_LOCK_CONTROL, false);

        SchedulerUtil.runLater(level.getServer(), ABORT_FADE_IN_TICKS, () -> drumIfStillLanded(console));
        return true;
    }

    private static boolean refuseToggle(
            ServerLevel level,
            BlockPos soundPos,
            Vec3 leverPos,
            TardisConsoleBlockEntity console,
            @Nullable Tardis tardis,
            boolean takingOff,
            @Nullable Player pilot) {
        if (!takingOff && tardis != null && QUEUED_TAKEOFFS.containsKey(tardis.id())) {
            QUEUED_TAKEOFFS.remove(tardis.id());
            ConsoleSounds.at(level, soundPos, GCSounds.SLIDER, 1.0f);
            console.setControlActivated(GCTardisControls.SPACE_TIME_THROTTLE, false);
            return true;
        }

        TardisExteriorBlockEntity exterior =
                tardis == null ? null : tardis.exterior().orElse(null);
        if (exterior == null) {
            return false;
        }

        if (exterior.travel().getFlightState().isTransitioning()) {
            return true;
        }

        if (!takingOff && !exterior.travel().isTravelling()) {
            ConsoleSounds.at(level, soundPos, GCSounds.SLIDER, 1.0f);
            console.setControlActivated(GCTardisControls.SPACE_TIME_THROTTLE, false);
            return true;
        }

        if (!takingOff && stillTravelling(level, console, soundPos, leverPos, tardis)) {
            return true;
        }
        if (takingOff
                && TardisRefusals.refuseTakeoff(
                        level,
                        console,
                        GCTardisControls.SPACE_TIME_THROTTLE,
                        soundPos,
                        leverPos,
                        tardis,
                        pilot == null ? null : pilot.getUUID())) {
            return true;
        }

        boolean handbrakeOn = console.getControlState(GCTardisControls.TIME_ROTOR_HANDBRAKE);
        if (takingOff && (handbrakeOn || !exterior.doors().areFullyClosed())) {
            ConsoleSounds.at(level, soundPos, GCSounds.SLIDER, 0.9f);
            console.setControlActivated(GCTardisControls.SPACE_TIME_THROTTLE, true);
            queueTakeoff(tardis);
            return true;
        }

        return false;
    }

    private static boolean stillTravelling(
            ServerLevel level, TardisConsoleBlockEntity console, BlockPos soundPos, Vec3 leverPos, Tardis tardis) {
        long ticksLeft = TardisTravel.remainingTicks(tardis);
        if (ticksLeft <= 0L) return false;

        ConsoleSounds.at(level, soundPos, GCSounds.SLIDER, 0.6f);
        console.setControlActivated(GCTardisControls.SPACE_TIME_THROTTLE, true);
        long secondsLeft = (ticksLeft + 19L) / 20L;
        MessageUtil.overlayNearby(level, leverPos, ChatFormatting.GOLD, "still_travelling", secondsLeft);
        return true;
    }

    private static void land(ServerLevel level, TardisConsoleBlockEntity console, @Nullable Tardis tardis) {
        stopSound(level, GCSounds.TARDIS_VWORP);

        if (tardis != null) {
            TardisLanding.relocate(tardis);
            tardis.setControlState(GCTardisControls.NAVIGATIONAL_LOCK_CONTROL, false);
            TardisEvents.LAND.fire(tardis);
        }

        ConsoleSounds.everywhere(console, GCSounds.TARDIS_LAND);
        SchedulerUtil.runLater(level.getServer(), LAND_SOUND_LENGTH_TICKS, () -> drumIfStillLanded(console));
    }

    private static void beginTakeoffEffects(ServerLevel level, TardisConsoleBlockEntity console, Vec3 shakePos) {
        ConsoleSounds.everywhere(console, GCSounds.TARDIS_DRUM);
        float pitch = Tardis.of(console)
                .map(tardis -> TardisTurbulence.vworpPitch(TardisMinds.stress(tardis)))
                .orElse(1.0f);
        for (TardisConsoleBlockEntity each : console.linkedConsoles()) {
            if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
            TimedSounds.playThenLoop(
                    eachLevel,
                    each.getBlockPos(),
                    GCSounds.TARDIS_TAKEOFF,
                    GCSounds.TARDIS_VWORP,
                    SoundSource.BLOCKS,
                    1.0f,
                    pitch,
                    VWORP_VOLUME);
        }
        ConsoleSounds.atExterior(console, GCSounds.TARDIS_TAKEOFF);
        // TODO: make the stabilizers play different takeoff sound and in flight sound
        Tardis.of(console).ifPresent(TardisEvents.TAKE_OFF::fire);

        for (TardisConsoleBlockEntity each : console.linkedConsoles()) {
            if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
            Vec3 pos = each == console ? shakePos : Vec3.atCenterOf(each.getBlockPos());
            if (!console.getControlState(GCTardisControls.GYROSCOPIC_STABILIZERS)) {
                TardisDrumShakePayload.broadcast(eachLevel, pos, 1.0f);
            }
        }
    }

    private static void queueTakeoff(Tardis tardis) {
        MinecraftServer server = tardis.server();
        UUID tardisId = tardis.id();
        int generation = ++queueGeneration;
        QUEUED_TAKEOFFS.put(tardisId, generation);
        SchedulerUtil.runLater(
                server, QUEUED_TAKEOFF_POLL_TICKS, () -> pollQueuedTakeoff(server, tardisId, generation));
    }

    private static void pollQueuedTakeoff(MinecraftServer server, UUID tardisId, int generation) {
        Integer current = QUEUED_TAKEOFFS.get(tardisId);
        if (current == null || current != generation) return;

        Optional<Tardis> tardis = Tardis.get(server, tardisId);
        TardisConsoleBlockEntity console = tardis.flatMap(Tardis::console).orElse(null);
        TardisExteriorBlockEntity exterior = tardis.flatMap(Tardis::exterior).orElse(null);
        if (console == null
                || exterior == null
                || !console.getControlState(GCTardisControls.SPACE_TIME_THROTTLE)
                || !(console.getLevel() instanceof ServerLevel level)) {
            QUEUED_TAKEOFFS.remove(tardisId);
            return;
        }

        if (exterior.doors().areFullyClosed() && !console.getControlState(GCTardisControls.TIME_ROTOR_HANDBRAKE)) {
            QUEUED_TAKEOFFS.remove(tardisId);

            throttleChanged(console, true);
            beginTakeoffEffects(level, console, Vec3.atCenterOf(console.getBlockPos()));
            return;
        }

        SchedulerUtil.runLater(
                server, QUEUED_TAKEOFF_POLL_TICKS, () -> pollQueuedTakeoff(server, tardisId, generation));
    }

    private static void stopSound(ServerLevel level, SoundEvent sound) {
        TimedSounds.stop(level, sound);
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(sound.location(), SoundSource.BLOCKS);
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    private static void scheduleLandingFinish(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) {
            console.triggerRotorAnimation("rotor_idle");
            return;
        }

        int fadeInTicks = Tardis.of(console)
                .flatMap(Tardis::exterior)
                .map(exterior -> exterior.travel().getFadeType().timing().fadeInTicks())
                .orElse(FadeTiming.DEFAULT.fadeInTicks());

        SchedulerUtil.runLater(level.getServer(), LAND_FADE_DELAY_TICKS + fadeInTicks, () -> {
            for (TardisConsoleBlockEntity each : landedConsoles(console)) {
                each.triggerRotorAnimation("rotor_idle");
            }
            announceArrival(level, console);
        });
    }

    private static void announceDeparture(ServerLevel interior) {
        MessageUtil.overlayAll(
                interior,
                Component.translatable("overlay.gallifreyan_chronicles.flight_departing")
                        .withStyle(ChatFormatting.GOLD));
    }

    private static void announceArrival(ServerLevel interior, TardisConsoleBlockEntity console) {
        Tardis tardis = Tardis.of(console).orElse(null);
        if (tardis == null) return;
        BlockPos pos = tardis.exteriorPos().orElse(null);
        if (pos == null) return;
        Component where = TardisDestinations.dimensionName(tardis.exteriorDimension());
        MessageUtil.overlayAll(
                interior,
                Component.translatable(
                                "overlay.gallifreyan_chronicles.flight_arrived",
                                where,
                                Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                                        .withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.GOLD));
    }

    private static List<TardisConsoleBlockEntity> landedConsoles(TardisConsoleBlockEntity console) {
        List<TardisConsoleBlockEntity> live = console.linkedConsoles();
        if (live.stream().anyMatch(each -> each.getControlState(GCTardisControls.SPACE_TIME_THROTTLE)))
            return List.of();
        return live;
    }

    private static void drumIfStillLanded(TardisConsoleBlockEntity console) {
        List<TardisConsoleBlockEntity> live = landedConsoles(console);
        if (live.isEmpty()) return;

        boolean stabilizersOn = console.getControlState(GCTardisControls.GYROSCOPIC_STABILIZERS);
        for (TardisConsoleBlockEntity each : live) {
            if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
            if (!stabilizersOn) {
                ConsoleSounds.at(eachLevel, each.getBlockPos(), GCSounds.TARDIS_DRUM, 1.0f);
                TardisDrumShakePayload.broadcast(eachLevel, Vec3.atCenterOf(each.getBlockPos()), 1.0f);
            }
        }
        if (!stabilizersOn) {
            ConsoleSounds.atExterior(console, GCSounds.TARDIS_DRUM);
        } else {
            ConsoleSounds.atConsole(console, GCSounds.TARDIS_LANDING_NOTIFICATION);
        }
    }
}
