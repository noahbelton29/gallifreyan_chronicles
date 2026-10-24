package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class HandbrakeControl implements TardisControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("handbrake_down", "handbrake_up", false);
    public static final MapCodec<HandbrakeControl> CODEC = ControlAnimations.codec(DEFAULT_ANIMATIONS)
            .xmap(HandbrakeControl::new, HandbrakeControl::animations);

    private final ControlAnimations animations;

    public HandbrakeControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public HandbrakeControl(ControlAnimations animations) {
        this.animations = animations;
    }

    private boolean STATE = false;

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
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

        Constants.LOG.info("clicked handbrake control");
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
    public MapCodec<HandbrakeControl> codec() {
        return CODEC;
    }

    public ControlAnimations animations() {
        return animations;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.info("Handbrake state changed to: {}", activated);

        STATE = activated;
    }
}