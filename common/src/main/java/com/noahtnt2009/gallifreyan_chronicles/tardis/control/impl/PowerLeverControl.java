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

public class PowerLeverControl implements TardisControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("power_lever_down", "power_lever_up", false);
    public static final MapCodec<PowerLeverControl> CODEC = ControlAnimations.codec(DEFAULT_ANIMATIONS)
            .xmap(PowerLeverControl::new, PowerLeverControl::animations);

    private final ControlAnimations animations;

    public PowerLeverControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public PowerLeverControl(ControlAnimations animations) {
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

        Constants.LOG.info("clicked power control");
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
    public MapCodec<PowerLeverControl> codec() {
        return CODEC;
    }

    public ControlAnimations animations() {
        return animations;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.info("Power state changed to: {}", activated);

        STATE = activated;
    }
}
