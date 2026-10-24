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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record DoorLockControl(ControlAnimations animations) implements TardisControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("door_lock_on", "door_lock_off", false);
    public static final MapCodec<DoorLockControl> CODEC = ControlAnimations.codec(DEFAULT_ANIMATIONS)
            .xmap(DoorLockControl::new, DoorLockControl::animations);

    public DoorLockControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public static final String ID = "door_lock";

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null || !(entity.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        TardisExteriorBlockEntity exterior = findExterior(console, serverLevel);
        if (exterior == null) {
            if (player != null) {
                player.sendOverlayMessage(
                        Component.literal("The door lock has no linked TARDIS exterior!").withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }

        // make sure the control agrees with the real lock before it gets toggled
        console.reconcileControl(ID, exterior.isLocked());

        boolean locking = !exterior.isLocked();
        if (locking && !exterior.areDoorsFullyClosed()) {
            if (player != null) {
                player.sendOverlayMessage(Component.literal("Close both doors before locking!"));
            }
            return InteractionResult.FAIL;
        }

        entity.level().playSound(
                null,
                entity.blockPosition(),
                GCSounds.SWITCH,
                SoundSource.BLOCKS,
                1.0F,
                locking ? 1.0f : 0.8f
        );

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
    public MapCodec<DoorLockControl> codec() {
        return CODEC;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.info("Door Lock state changed to: {}", activated);

        if (!(console.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        TardisExteriorBlockEntity exterior = findExterior(console, serverLevel);
        if (exterior == null) {
            Constants.LOG.warn("Door Lock at {} has no linked TARDIS, lock not changed", console.getBlockPos());
            return;
        }

        exterior.setLockedFromConsole(activated);
    }

    private static @Nullable TardisExteriorBlockEntity findExterior(TardisConsoleBlockEntity console,
                                                                    ServerLevel serverLevel) {
        UUID tardisId = console.getTardisId();
        if (tardisId == null) {
            tardisId = DoorSyncSystem.tardisIdOf(serverLevel);
        }
        if (tardisId == null) {
            return null;
        }
        return DoorSyncSystem.findExterior(serverLevel.getServer(), tardisId);
    }
}
