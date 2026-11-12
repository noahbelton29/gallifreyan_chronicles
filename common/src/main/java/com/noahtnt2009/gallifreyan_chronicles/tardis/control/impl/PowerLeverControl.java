package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisCrash;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class PowerLeverControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("power_lever_down", "power_lever_up", false);
    public static final MapCodec<PowerLeverControl> CODEC = codec(DEFAULT_ANIMATIONS, PowerLeverControl::new);

    public PowerLeverControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public PowerLeverControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        super.playClickSound(entity);
        var console = entity.getConsole();
        if (console == null) return;
        playSound(entity, console.getControlState(GCTardisControls.POWER_LEVER)
                ? GCSounds.TARDIS_POWER_OFF : GCSounds.TARDIS_POWER_ON, 1.0f);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;
        if (linkedTardis(console).map(TardisCrash::isCrashing).orElse(false)) {
            tell(player, "message.gallifreyan_chronicles.power_locked_crashing");
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public MapCodec<PowerLeverControl> codec() {
        return CODEC;
    }
}
