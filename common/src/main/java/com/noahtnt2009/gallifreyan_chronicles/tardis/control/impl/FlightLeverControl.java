package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.noahtnt2009.gallifreyan_chronicles.sound.TimedSounds;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisDrumShakePayload;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLandingSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTiming;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FlightLeverControl extends BaseControl {
    public static final String ID = "flight_lever";
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("flight_lever_down", "flight_lever_up", false);
    public static final MapCodec<FlightLeverControl> CODEC = codec(DEFAULT_ANIMATIONS,
            FlightLeverControl::new);

    public FlightLeverControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public FlightLeverControl(ControlAnimations animations) {
        super(animations);
    }

    private static final float VWORP_VOLUME = 0.35F;
    private static final int TAKEOFF_SOUND_DELAY_TICKS = 0;
    private static final int TAKEOFF_SOUND_LENGTH_TICKS = 425;

    private static final Map<UUID, Integer> QUEUED_TAKEOFFS = new HashMap<>();
    private static int queueGeneration = 0;
    private static final int QUEUED_TAKEOFF_POLL_TICKS = 2;
    private static final int LAND_FADE_DELAY_TICKS = 130;
    private static final int LAND_SOUND_LENGTH_TICKS = 393;
    private static final int ABORT_FADE_IN_TICKS = 30;

    public static void resetState() {
        QUEUED_TAKEOFFS.clear();
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
        }

        String controlId = entity.getControlId();
        boolean takingOff = !console.getControlState(controlId);

        if (entity.level() instanceof ServerLevel level) {
            UUID tardisId = tardis(console).map(Tardis::id).orElse(null);

            if (refuseToggle(level, entity.blockPosition(), console, tardisId, takingOff)) {
                return InteractionResult.FAIL;
            }

            playSound(entity, GCSounds.SLIDER, 0.9f);

            if (takingOff) {
                beginTakeoffEffects(level, console, controlId, entity.position());
            } else {
                land(level, console, tardisId);
            }
            return InteractionResult.SUCCESS;
        }

        playSound(entity, GCSounds.SLIDER, 0.9f);
        return InteractionResult.SUCCESS;
    }

    private static boolean refuseToggle(
            ServerLevel level,
            BlockPos soundPos,
            TardisConsoleBlockEntity console,
            @Nullable UUID tardisId,
            boolean takingOff
    ) {
        String controlId = ID;

        if (!takingOff && tardisId != null && QUEUED_TAKEOFFS.containsKey(tardisId)) {
            QUEUED_TAKEOFFS.remove(tardisId);
            playAt(level, soundPos, GCSounds.SLIDER, 1.0f);
            console.setControlActivated(controlId, false);
            return true;
        }

        TardisExteriorBlockEntity exterior = TardisApi.get(level.getServer(), tardisId)
                .flatMap(Tardis::exterior).orElse(null);
        if (exterior == null) {
            return false;
        }

        if (exterior.getFlightState().isTransitioning()) {
            return true;
        }

        if (!takingOff && !exterior.isTravelling()) {
            playAt(level, soundPos, GCSounds.SLIDER, 1.0f);
            console.setControlActivated(controlId, false);
            return true;
        }

        boolean handbrakeOn = console.getControlState(GCTardisControls.HANDBRAKE);
        if (takingOff && (handbrakeOn || !exterior.areDoorsFullyClosed())) {
            playAt(level, soundPos, GCSounds.SLIDER, 0.9f);
            console.setControlActivated(controlId, true);
            queueTakeoff(level.getServer(), tardisId, controlId);
            return true;
        }

        return false;
    }

    public static boolean pull(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) return false;
        UUID tardisId = tardis(console).map(Tardis::id).orElse(null);
        BlockPos soundPos = console.getBlockPos();
        boolean takingOff = !console.getControlState(ID);

        if (refuseToggle(level, soundPos, console, tardisId, takingOff)) return false;

        playAt(level, soundPos, GCSounds.SLIDER, takingOff ? 0.9f : 1.0f);
        if (takingOff) {
            beginTakeoffEffects(level, console, ID, Vec3.atCenterOf(soundPos));
        } else {
            land(level, console, tardisId);
        }
        console.triggerControl(ID, takingOff, null);
        return true;
    }

    private static void land(ServerLevel level, TardisConsoleBlockEntity console, @Nullable UUID tardisId) {
        stopVworp(level);

        if (tardisId != null) {
            TardisLandingSystem.relocate(level.getServer(), tardisId);
            TardisApi.get(level.getServer(), tardisId).ifPresent(TardisEvents.LAND::fire);
        }

        playEverywhere(console, level, GCSounds.TARDIS_LAND);
        scheduleLandDrum(level, console);
    }

    private static void beginTakeoffEffects(
            ServerLevel level,
            TardisConsoleBlockEntity console,
            String controlId,
            Vec3 shakePos
    ) {
        playEverywhere(console, level, GCSounds.TARDIS_DRUM);
        for (TardisConsoleBlockEntity each : ConsoleSyncSystem.liveConsolesWith(console)) {
            if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
            TimedSounds.playThenLoop(eachLevel, each.getBlockPos(), GCSounds.TARDIS_TAKEOFF, GCSounds.TARDIS_VWORP,
                    SoundSource.BLOCKS, 1.0F, 1.0F, VWORP_VOLUME);
        }
        playAtExterior(console, level, GCSounds.TARDIS_TAKEOFF);
        tardis(console).ifPresent(TardisEvents.TAKE_OFF::fire);

        for (TardisConsoleBlockEntity each : ConsoleSyncSystem.liveConsolesWith(console)) {
            if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
            Vec3 pos = each == console ? shakePos : Vec3.atCenterOf(each.getBlockPos());
            TardisDrumShakePayload.broadcast(eachLevel, pos, 1.0F);
        }
    }

    private static void queueTakeoff(MinecraftServer server, UUID tardisId, String controlId) {
        int generation = ++queueGeneration;
        QUEUED_TAKEOFFS.put(tardisId, generation);
        GCScheduler.runLater(server, QUEUED_TAKEOFF_POLL_TICKS,
                () -> pollQueuedTakeoff(server, tardisId, controlId, generation));
    }

    private static void pollQueuedTakeoff(MinecraftServer server, UUID tardisId, String controlId, int generation) {
        Integer current = QUEUED_TAKEOFFS.get(tardisId);
        if (current == null || current != generation) return;

        Optional<Tardis> tardis = TardisApi.get(server, tardisId);
        TardisConsoleBlockEntity console = tardis.flatMap(Tardis::console).orElse(null);
        TardisExteriorBlockEntity exterior = tardis.flatMap(Tardis::exterior).orElse(null);
        if (console == null || exterior == null || !console.getControlState(controlId)
                || !(console.getLevel() instanceof ServerLevel level)) {
            QUEUED_TAKEOFFS.remove(tardisId);
            return;
        }

        if (exterior.areDoorsFullyClosed() && !console.getControlState(GCTardisControls.HANDBRAKE)) {
            QUEUED_TAKEOFFS.remove(tardisId);

            applyLeverState(console, true);
            beginTakeoffEffects(level, console, controlId, Vec3.atCenterOf(console.getBlockPos()));
            return;
        }

        GCScheduler.runLater(server, QUEUED_TAKEOFF_POLL_TICKS,
                () -> pollQueuedTakeoff(server, tardisId, controlId, generation));
    }

    private static void stopVworp(ServerLevel level) {
        stopSound(level, GCSounds.TARDIS_VWORP);
    }

    private static void stopSound(ServerLevel level, SoundEvent sound) {
        TimedSounds.stop(level, sound);
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(sound.location(), SoundSource.BLOCKS);
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    public static boolean abortTakeoff(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) return false;
        Optional<Tardis> tardis = tardis(console);
        TardisExteriorBlockEntity exterior = tardis.flatMap(Tardis::exterior).orElse(null);
        if (exterior == null || exterior.getFlightState() != TardisFlightState.TAKING_OFF) return false;

        QUEUED_TAKEOFFS.remove(tardis.get().id());
        List<TardisConsoleBlockEntity> consoles = ConsoleSyncSystem.liveConsolesWith(console);

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

        playEverywhere(console, level, GCSounds.STOP_DEMAT);
        exterior.abortTakeoff(ABORT_FADE_IN_TICKS);

        for (TardisConsoleBlockEntity each : consoles) {
            each.setControlActivated(ID, false);
            each.triggerRotorAnimation("rotor_idle");
        }

        GCScheduler.runLater(level.getServer(), ABORT_FADE_IN_TICKS, () -> {
            List<TardisConsoleBlockEntity> live = ConsoleSyncSystem.liveConsolesWith(console);
            if (live.isEmpty() || live.stream().anyMatch(each -> each.getControlState(ID))) return;

            for (TardisConsoleBlockEntity each : live) {
                if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
                playAt(eachLevel, each.getBlockPos(), GCSounds.TARDIS_DRUM, 1.0F);
                TardisDrumShakePayload.broadcast(eachLevel, Vec3.atCenterOf(each.getBlockPos()), 1.0F);
            }
            playAtExterior(console, level, GCSounds.TARDIS_DRUM);
        });
        return true;
    }

    @Override
    public MapCodec<FlightLeverControl> codec() {
        return CODEC;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        applyLeverState(console, activated);
    }

    private static void applyLeverState(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.debug("Flight lever state changed to: {}", activated);

        if (console.getLevel() instanceof ServerLevel serverLevel) {
            TardisExteriorBlockEntity exterior = tardis(console).flatMap(Tardis::exterior).orElse(null);
            if (exterior != null) {
                if (activated) {
                    exterior.cancelLanding();
                    exterior.setInFlight(true);
                    exterior.setFlightState(TardisFlightState.TAKING_OFF,
                            TAKEOFF_SOUND_DELAY_TICKS + TAKEOFF_SOUND_LENGTH_TICKS);
                } else {
                    exterior.scheduleLanding(LAND_FADE_DELAY_TICKS);
                    exterior.setFlightState(TardisFlightState.LANDING,
                            LAND_FADE_DELAY_TICKS + exterior.getFadeType().timing().fadeInTicks());
                }
            }
        }

        if (activated) {
            for (TardisConsoleBlockEntity each : ConsoleSyncSystem.liveConsolesWith(console)) {
                each.triggerRotorAnimation("rotor_flight");
            }
        } else {
            scheduleLandingFinish(console);
        }
    }

    private static void scheduleLandingFinish(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) {
            console.triggerRotorAnimation("rotor_idle");
            return;
        }

        int fadeInTicks = TardisApi.of(console)
                .flatMap(Tardis::exterior)
                .map(exterior -> exterior.getFadeType().timing().fadeInTicks())
                .orElse(FadeTiming.DEFAULT.fadeInTicks());

        GCScheduler.runLater(level.getServer(), LAND_FADE_DELAY_TICKS + fadeInTicks, () -> {
            List<TardisConsoleBlockEntity> live = ConsoleSyncSystem.liveConsolesWith(console);
            if (live.isEmpty() || live.stream().anyMatch(each -> each.getControlState(ID))) return;

            for (TardisConsoleBlockEntity each : live) {
                each.triggerRotorAnimation("rotor_idle");
            }
        });
    }

    private static void scheduleLandDrum(ServerLevel level, TardisConsoleBlockEntity console) {
        GCScheduler.runLater(level.getServer(), LAND_SOUND_LENGTH_TICKS, () -> {
            List<TardisConsoleBlockEntity> live = ConsoleSyncSystem.liveConsolesWith(console);
            if (live.isEmpty() || live.stream().anyMatch(each -> each.getControlState(ID))) return;

            for (TardisConsoleBlockEntity each : live) {
                if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
                playAt(eachLevel, each.getBlockPos(), GCSounds.TARDIS_DRUM, 1.0F);
                TardisDrumShakePayload.broadcast(eachLevel, Vec3.atCenterOf(each.getBlockPos()), 1.0F);
            }
            playAtExterior(console, level, GCSounds.TARDIS_DRUM);
        });
    }
}
