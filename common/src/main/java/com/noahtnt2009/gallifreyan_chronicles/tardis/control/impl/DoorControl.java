package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DoorControl implements TardisControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("door_control_off", "door_control_on", true);
    public static final MapCodec<DoorControl> CODEC = ControlAnimations.codec(DEFAULT_ANIMATIONS)
            .xmap(DoorControl::new, DoorControl::animations);

    private final ControlAnimations animations;

    public DoorControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public DoorControl(ControlAnimations animations) {
        this.animations = animations;
    }

    private boolean STATE = false;

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            UUID tardisId = console.getTardisId();
            if (tardisId == null) {
                tardisId = DoorSyncSystem.tardisIdOf(serverLevel);
            }
            TardisExteriorBlockEntity exterior =
                    tardisId == null ? null : DoorSyncSystem.findExterior(serverLevel.getServer(), tardisId);
            if (exterior != null && exterior.isLocked()) {
                if (player != null) {
                    player.sendOverlayMessage(
                            Component.literal("The doors are locked!").withStyle(ChatFormatting.RED));
                }
                return InteractionResult.FAIL;
            }
            if (exterior != null && exterior.areDoorsSealedForTravel()) {
                DoorSyncSystem.announceSealed(tardisId);
                if (player != null) {
                    player.sendOverlayMessage(
                            Component.literal("The doors are sealed while the TARDIS is travelling!")
                                    .withStyle(ChatFormatting.RED));
                }
                return InteractionResult.FAIL;
            }
        }

        SoundEvent sound = GCSounds.SWITCH;

        entity.level().playSound(
                null,
                entity.blockPosition(),
                sound,
                SoundSource.BLOCKS,
                1.0F,
                STATE ? 1.0f : 0.8f
        );

        // TODO: play at console pos for all tardis sounds

        Constants.LOG.info("clicked door control");
        return InteractionResult.SUCCESS;
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
    public MapCodec<DoorControl> codec() {
        return CODEC;
    }

    public ControlAnimations animations() {
        return animations;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.info("Door Control state changed to: {}", activated);

        STATE = activated;

        if (!(console.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID tardisId = console.getTardisId();
        if (tardisId == null) {
            tardisId = DoorSyncSystem.tardisIdOf(serverLevel);
        }
        if (tardisId == null) {
            Constants.LOG.warn("Door Control at {} has no linked TARDIS, doors not toggled", console.getBlockPos());
            return;
        }

        DoorSyncSystem.toggleAllDoors(serverLevel.getServer(), tardisId);
    }
}
