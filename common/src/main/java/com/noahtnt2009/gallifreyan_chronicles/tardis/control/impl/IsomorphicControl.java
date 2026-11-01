package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class IsomorphicControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("isomorphic_control_on", "isomorphic_control_off", false);
    public static final MapCodec<IsomorphicControl> CODEC = codec(DEFAULT_ANIMATIONS, IsomorphicControl::new);
    private static final int SECOND_SOUND_DELAY_TICKS = 10;

    public IsomorphicControl() { this(DEFAULT_ANIMATIONS); }
    public IsomorphicControl(ControlAnimations animations) { super(animations); }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        TardisConsoleBlockEntity console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;

        float pitch = console.getControlState(entity.getControlId()) ? 1.0F : 0.8F;
        BlockPos pos = entity.blockPosition();
        if (entity.level() instanceof ServerLevel level) {
            playAt(level, pos, GCSounds.SWITCH, pitch);
            GCScheduler.runLater(level.getServer(), SECOND_SOUND_DELAY_TICKS,
                    () -> playAt(level, pos, GCSounds.SWITCH, pitch));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public MapCodec<? extends TardisControl> codec() {
        return CODEC;
    }
}
