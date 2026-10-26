package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisDrumShakePayload;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightLeverControl implements TardisControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("flight_lever_down", "flight_lever_up", false);
    public static final MapCodec<FlightLeverControl> CODEC = ControlAnimations.codec(DEFAULT_ANIMATIONS)
            .xmap(FlightLeverControl::new, FlightLeverControl::animations);

    private final ControlAnimations animations;

    public FlightLeverControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public FlightLeverControl(ControlAnimations animations) {
        this.animations = animations;
    }

    private static final int TAKEOFF_SOUND_DELAY_TICKS = 0;
    private static final int TAKEOFF_SOUND_LENGTH_TICKS = 425;
    private static final int VWORP_LOOP_TICKS = 273;

    private static final Map<GlobalPos, Integer> VWORP_LOOPS = new HashMap<>();
    private static final Map<UUID, Integer> QUEUED_TAKEOFFS = new HashMap<>();
    private static int queueGeneration = 0;
    private static final int QUEUED_TAKEOFF_POLL_TICKS = 2;
    private static final int LAND_FADE_DELAY_TICKS = 130;

    private boolean STATE = false;

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
        }

        boolean takingOff = !console.getControlState(entity.getControlId());

        if (entity.level() instanceof ServerLevel level) {
            UUID tardisId = tardisIdOf(console, level);

            if (!takingOff && tardisId != null && QUEUED_TAKEOFFS.containsKey(tardisId)) {
                QUEUED_TAKEOFFS.remove(tardisId);
                playSlider(level, entity.blockPosition(), 1.0f);
                console.setControlActivated(entity.getControlId(), false);
                return InteractionResult.FAIL;
            }

            TardisExteriorBlockEntity exterior = findExterior(console, level);
            if (exterior != null) {
                if (exterior.getFlightState().isTransitioning()) {
                    return InteractionResult.FAIL;
                }

                if (!takingOff && !exterior.isTravelling()) {
                    playSlider(level, entity.blockPosition(), 1.0f);
                    console.setControlActivated(entity.getControlId(), false);
                    return InteractionResult.FAIL;
                }

                if (takingOff && !exterior.areDoorsFullyClosed() && tardisId != null) {
                    playSlider(level, entity.blockPosition(), 0.9f);
                    console.setControlActivated(entity.getControlId(), true);
                    queueTakeoff(level.getServer(), tardisId, entity.getControlId());
                    return InteractionResult.FAIL;
                }
            }
        }

        SoundEvent sound = GCSounds.SLIDER;

        entity.level().playSound(
                null,
                entity.blockPosition(),
                sound,
                SoundSource.BLOCKS,
                1.0F,
                STATE ? 1.0f : 0.9f
        );

        if (takingOff && entity.level() instanceof ServerLevel level) {
            beginTakeoffEffects(level, console, entity.getControlId(), console.getBlockPos(), entity.position());
        }

        if (!takingOff && entity.level() instanceof ServerLevel level) {
            VWORP_LOOPS.merge(GlobalPos.of(level.dimension(), console.getBlockPos()), 1, Integer::sum);
            stopVworp(level);

            UUID landingId = console.getTardisId() != null ? console.getTardisId() : DoorSyncSystem.tardisIdOf(level);
            if (landingId != null) {
                TardisLandingSystem.relocate(level.getServer(), landingId);
            }

            level.playSound(null, console.getBlockPos(), GCSounds.TARDIS_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
            playAtExterior(console, level, GCSounds.TARDIS_LAND);
        }

        Constants.LOG.info("clicked flight control");
        return InteractionResult.SUCCESS;
    }

    private void beginTakeoffEffects(
            ServerLevel level,
            TardisConsoleBlockEntity console,
            String controlId,
            BlockPos soundPos,
            Vec3 shakePos
    ) {
        level.playSound(
                null,
                soundPos,
                GCSounds.TARDIS_DRUM,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        level.playSound(
                null,
                soundPos,
                GCSounds.TARDIS_TAKEOFF,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        TardisDrumShakePayload.broadcast(level, shakePos, 1.0F);

        playAtExterior(console, level, GCSounds.TARDIS_DRUM);
        playAtExterior(console, level, GCSounds.TARDIS_TAKEOFF);

        startVworpLoop(level, console, controlId);
    }


    private static void playAtExterior(
            TardisConsoleBlockEntity console,
            ServerLevel consoleLevel,
            SoundEvent sound
    ) {
        TardisExteriorBlockEntity exterior = findExterior(console, consoleLevel);

        if (exterior == null ||
                !(exterior.getLevel() instanceof ServerLevel exteriorLevel)) {
            return;
        }

        exteriorLevel.playSound(
                null,
                Vec3.atCenterOf(exterior.getBlockPos()).x,
                Vec3.atCenterOf(exterior.getBlockPos()).y,
                Vec3.atCenterOf(exterior.getBlockPos()).z,
                sound,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }

    private static @Nullable TardisExteriorBlockEntity findExterior(TardisConsoleBlockEntity console,
                                                                    ServerLevel level) {
        UUID tardisId = console.getTardisId();
        if (tardisId == null) {
            tardisId = DoorSyncSystem.tardisIdOf(level);
        }
        if (tardisId == null) return null;
        return DoorSyncSystem.findExterior(level.getServer(), tardisId);
    }

    private static void playSlider(ServerLevel level, BlockPos pos, float pitch) {
        level.playSound(null, pos, GCSounds.SLIDER, SoundSource.BLOCKS, 1.0F, pitch);
    }

    private static @Nullable UUID tardisIdOf(TardisConsoleBlockEntity console, ServerLevel level) {
        UUID tardisId = console.getTardisId();
        return tardisId != null ? tardisId : DoorSyncSystem.tardisIdOf(level);
    }

    private static @Nullable TardisConsoleBlockEntity resolveConsole(MinecraftServer server, UUID tardisId) {
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

        if (exterior.areDoorsFullyClosed()) {
            QUEUED_TAKEOFFS.remove(tardisId);

            BlockPos pos = console.getBlockPos();
            onStateChanged(console, true);
            beginTakeoffEffects(level, console, controlId, pos, Vec3.atCenterOf(pos));
            return;
        }

        GCScheduler.runLater(server, QUEUED_TAKEOFF_POLL_TICKS,
                () -> pollQueuedTakeoff(server, tardisId, controlId, generation));
    }

    private void startVworpLoop(ServerLevel level, TardisConsoleBlockEntity console, String controlId) {
        GlobalPos key = GlobalPos.of(level.dimension(), console.getBlockPos());
        int generation = VWORP_LOOPS.merge(key, 1, Integer::sum);
        BlockPos pos = console.getBlockPos();

        GCScheduler.runLater(level.getServer(), TAKEOFF_SOUND_DELAY_TICKS + TAKEOFF_SOUND_LENGTH_TICKS,
                () -> vworpStep(level, pos, key, generation, console, controlId));
    }

    private void vworpStep(ServerLevel level, BlockPos pos, GlobalPos key, int generation,
                           TardisConsoleBlockEntity console, String controlId) {
        if (VWORP_LOOPS.getOrDefault(key, 0) != generation || !console.getControlState(controlId)) return;

        level.playSound(null, pos, GCSounds.TARDIS_VWORP, SoundSource.BLOCKS, 1.0F, 1.0F);
        GCScheduler.runLater(level.getServer(), VWORP_LOOP_TICKS,
                () -> vworpStep(level, pos, key, generation, console, controlId));
    }

    private static void stopVworp(ServerLevel level) {
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(GCSounds.TARDIS_VWORP.location(), SoundSource.BLOCKS);
        for (ServerPlayer player : level.players()) {
            player.connection.send(packet);
        }
    }

    private static void playAfter(ServerLevel level, BlockPos pos, int delayTicks, SoundEvent sound) {
        if (delayTicks <= 0) {
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        GCScheduler.runLater(level.getServer(), delayTicks, () ->
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F));
    }

    @Override
    public boolean onLeftClick(TardisControlEntity entity, Player player) {
        return true;
    }

    @Override
    public String getAnimation(boolean activated) {
        return activated ? animations.active() : animations.inactive();
    }

    @Override
    public boolean snapsWhenInactive() {
        return animations.snapsWhenInactive();
    }

    @Override
    public MapCodec<FlightLeverControl> codec() {
        return CODEC;
    }

    public ControlAnimations animations() {
        return animations;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.info("Flight lever state changed to: {}", activated);

        STATE = activated;

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
            console.triggerRotorAnimation("rotor_flight");
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

        BlockPos pos = console.getBlockPos();
        GCScheduler.runLater(level.getServer(), LAND_FADE_DELAY_TICKS + fadeInTicks, () -> {
            if (console.getControlState("flight_lever")) return;

            console.triggerRotorAnimation("rotor_idle");
            level.playSound(null, pos, GCSounds.TARDIS_LAND_DRUM, SoundSource.BLOCKS, 1.0F, 1.0F);
            playAtExterior(console, level, GCSounds.TARDIS_LAND_DRUM);
            TardisDrumShakePayload.broadcast(level, Vec3.atCenterOf(pos), 1.0F);
        });
    }
}