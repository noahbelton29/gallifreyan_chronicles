package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class CloakControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("cloak_control_on", "cloak_control_off", true);
    public static final MapCodec<CloakControl> CODEC = codec(DEFAULT_ANIMATIONS, CloakControl::new);

    public CloakControl(ControlAnimations animations) {
        super(animations);
    }

    public CloakControl() {
        this(DEFAULT_ANIMATIONS);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
        }
        SoundEvent sound = GCSounds.LEVER;
        playSound(entity, sound, 0.8f);

        SoundEvent cloakSound = console.getControlState(GCTardisControls.CLOAK_CONTROL) ? GCSounds.TARDIS_UNCLOAK : GCSounds.TARDIS_CLOAK;
        playEverywhere(console, serverLevel, cloakSound);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        tardis(console).flatMap(Tardis::exterior).ifPresent(exterior -> exterior.setCloaked(activated));
    }

    @Override
    public MapCodec<? extends TardisControl> codec() {
        return CODEC;
    }
}
