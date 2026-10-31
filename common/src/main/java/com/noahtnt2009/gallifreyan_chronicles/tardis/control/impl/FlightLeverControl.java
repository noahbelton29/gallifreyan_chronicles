package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

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
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLandingSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTiming;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlightLeverControl extends BaseControl {
    public static final String ID = "flight_lever";
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("flight_lever_down", "flight_lever_up", false);
    public static final MapCodec<FlightLeverControl> CODEC = codec(DEFAULT_ANIMATIONS,
            FlightLeverControl::new);

    public FlightLeverControl() { this(DEFAULT_ANIMATIONS); }
    public FlightLeverControl(ControlAnimations animations) { super(animations); }

    private static final int TAKEOFF_SOUND_DELAY_TICKS = 0;
    private static final int TAKEOFF_SOUND_LENGTH_TICKS = 425;
    private static final int VWORP_LOOP_TICKS = 273;

    private static final Map<GlobalPos, Integer> VWORP_LOOPS = new HashMap<>();
    private static final Map<UUID, Integer> QUEUED_TAKEOFFS = new HashMap<>();
    private static int queueGeneration = 0;
    private static int vworpGeneration = 0;
    private static final int QUEUED_TAKEOFF_POLL_TICKS = 2;
    private static final int LAND_FADE_DELAY_TICKS = 130;

    public static void resetState() {
        VWORP_LOOPS.clear();
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
            UUID tardisId = tardisId(console, level);

            if (refuseToggle(level, entity, console, tardisId, takingOff)) {
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

    private boolean refuseToggle(
            ServerLevel level,
            TardisControlEntity entity,
            TardisConsoleBlockEntity console,
            @Nullable UUID tardisId,
            boolean takingOff
    ) {
        String controlId = entity.getControlId();

        if (!takingOff && tardisId != null && QUEUED_TAKEOFFS.containsKey(tardisId)) {
            QUEUED_TAKEOFFS.remove(tardisId);
            playAt(level, entity.blockPosition(), GCSounds.SLIDER, 1.0f);
            console.setControlActivated(controlId, false);
            return true;
        }

        TardisExteriorBlockEntity exterior = tardisId == null
                ? null
                : DoorSyncSystem.findExterior(level.getServer(), tardisId);
        if (exterior == null) {
            return false;
        }

        if (exterior.getFlightState().isTransitioning()) {
            return true;
        }

        if (!takingOff && !exterior.isTravelling()) {
            playAt(level, entity.blockPosition(), GCSounds.SLIDER, 1.0f);
            console.setControlActivated(controlId, false);
            return true;
        }

        boolean handbrakeOn = console.getControlState(GCTardisControls.HANDBRAKE);
        if (takingOff && (handbrakeOn || !exterior.areDoorsFullyClosed())) {
            playAt(level, entity.blockPosition(), GCSounds.SLIDER, 0.9f);
            console.setControlActivated(controlId, true);
            queueTakeoff(level.getServer(), tardisId, controlId);
            return true;
        }

        return false;
    }

    private static void land(ServerLevel level, TardisConsoleBlockEntity console, @Nullable UUID tardisId) {
        VWORP_LOOPS.remove(GlobalPos.of(level.dimension(), console.getBlockPos()));
        stopVworp(level);

        if (tardisId != null) {
            TardisLandingSystem.relocate(level.getServer(), tardisId);
        }

        playEverywhere(console, level, GCSounds.TARDIS_LAND);
    }

    private void beginTakeoffEffects(
            ServerLevel level,
            TardisConsoleBlockEntity console,
            String controlId,
            Vec3 shakePos
    ) {
        playEverywhere(console, level, GCSounds.TARDIS_DRUM);
        playEverywhere(console, level, GCSounds.TARDIS_TAKEOFF);

        for (TardisConsoleBlockEntity each : ConsoleSyncSystem.liveConsolesWith(console)) {
            if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
            Vec3 pos = each == console ? shakePos : Vec3.atCenterOf(each.getBlockPos());
            TardisDrumShakePayload.broadcast(eachLevel, pos, 1.0F);
        }

        startVworpLoop(level, console, controlId);
    }

    private static @Nullable TardisConsoleBlockEntity resolveConsole(MinecraftServer server, UUID tardisId) {
        List<TardisConsoleBlockEntity> live = ConsoleSyncSystem.consolesOf(tardisId);
        if (!live.isEmpty()) return live.getFirst();

        ServerLevel interior = TardisDimensionManager.get(server, tardisId);
        BlockPos pos = TardisManager.get(server).getConsoleBlockPos(tardisId);
        if (interior == null || pos == null) return null;
        interior.getChunkAt(pos);
        return interior.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console ? console : null;
    }

    private void queueTakeoff(MinecraftServer server, UUID tardisId, String controlId) {
        int generation = ++queueGeneration;
        QUEUED_TAKEOFFS.put(tardisId, generation);
        GCScheduler.runLater(server, QUEUED_TAKEOFF_POLL_TICKS,
                () -> pollQueuedTakeoff(server, tardisId, controlId, generation));
    }

    private void pollQueuedTakeoff(MinecraftServer server, UUID tardisId, String controlId, int generation) {
        Integer current = QUEUED_TAKEOFFS.get(tardisId);
        if (current == null || current != generation) return;

        TardisConsoleBlockEntity console = resolveConsole(server, tardisId);
        TardisExteriorBlockEntity exterior = DoorSyncSystem.findExterior(server, tardisId);
        if (console == null || exterior == null || !console.getControlState(controlId)
                || !(console.getLevel() instanceof ServerLevel level)) {
            QUEUED_TAKEOFFS.remove(tardisId);
            return;
        }

        if (exterior.areDoorsFullyClosed() && !console.getControlState(GCTardisControls.HANDBRAKE)) {
            QUEUED_TAKEOFFS.remove(tardisId);

            onStateChanged(console, true);
            beginTakeoffEffects(level, console, controlId, Vec3.atCenterOf(console.getBlockPos()));
            return;
        }

        GCScheduler.runLater(server, QUEUED_TAKEOFF_POLL_TICKS,
                () -> pollQueuedTakeoff(server, tardisId, controlId, generation));
    }

    private void startVworpLoop(ServerLevel level, TardisConsoleBlockEntity console, String controlId) {
        GlobalPos key = GlobalPos.of(level.dimension(), console.getBlockPos());
        int generation = ++vworpGeneration;
        VWORP_LOOPS.put(key, generation);

        GCScheduler.runLater(level.getServer(), TAKEOFF_SOUND_DELAY_TICKS + TAKEOFF_SOUND_LENGTH_TICKS,
                () -> vworpStep(level, key, generation, console, controlId));
    }

    private void vworpStep(ServerLevel level, GlobalPos key, int generation,
                           TardisConsoleBlockEntity console, String controlId) {
        Integer current = VWORP_LOOPS.get(key);
        if (current == null || current != generation) return;

        List<TardisConsoleBlockEntity> live = ConsoleSyncSystem.liveConsolesWith(console);
        if (live.stream().noneMatch(each -> each.getControlState(controlId))) {
            VWORP_LOOPS.remove(key, generation);
            return;
        }

        for (TardisConsoleBlockEntity each : live) {
            playAt(each.getLevel() instanceof ServerLevel eachLevel ? eachLevel : level,
                    each.getBlockPos(), GCSounds.TARDIS_VWORP, 1.0F);
        }
        GCScheduler.runLater(level.getServer(), VWORP_LOOP_TICKS,
                () -> vworpStep(level, key, generation, console, controlId));
    }

    private static void stopVworp(ServerLevel level) {
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(GCSounds.TARDIS_VWORP.location(), SoundSource.BLOCKS);
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    @Override
    public MapCodec<FlightLeverControl> codec() { return CODEC; }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.info("Flight lever state changed to: {}", activated);

        if (console.getLevel() instanceof ServerLevel serverLevel) {
            TardisExteriorBlockEntity exterior = findExterior(console, serverLevel);
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

    private void scheduleLandingFinish(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) {
            console.triggerRotorAnimation("rotor_idle");
            return;
        }

        int fadeInTicks = console.getLinkedTardisId()
                .map(tardisId -> DoorSyncSystem.findExterior(level.getServer(), tardisId))
                .map(exterior -> exterior.getFadeType().timing().fadeInTicks())
                .orElse(FadeTiming.DEFAULT.fadeInTicks());

        GCScheduler.runLater(level.getServer(), LAND_FADE_DELAY_TICKS + fadeInTicks, () -> {
            List<TardisConsoleBlockEntity> live = ConsoleSyncSystem.liveConsolesWith(console);
            if (live.isEmpty() || live.stream().anyMatch(each -> each.getControlState(ID))) return;

            for (TardisConsoleBlockEntity each : live) {
                if (!(each.getLevel() instanceof ServerLevel eachLevel)) continue;
                each.triggerRotorAnimation("rotor_idle");
                playAt(eachLevel, each.getBlockPos(), GCSounds.TARDIS_LAND_DRUM, 1.0F);
                TardisDrumShakePayload.broadcast(eachLevel, Vec3.atCenterOf(each.getBlockPos()), 1.0F);
            }
            playAtExterior(console, level, GCSounds.TARDIS_LAND_DRUM);
        });
    }
}
