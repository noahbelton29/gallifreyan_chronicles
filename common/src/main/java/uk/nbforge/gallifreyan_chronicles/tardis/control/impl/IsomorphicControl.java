package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class IsomorphicControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("isomorphic_control_on", "isomorphic_control_off", false);
    private static final int SECOND_SOUND_DELAY_TICKS = 10;

    public IsomorphicControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public IsomorphicControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        var console = entity.getConsole();
        if (console == null || !(entity.level() instanceof ServerLevel level)) return;
        float pitch = console.getControlState(entity.getControlId()) ? 1.0F : 0.8F;
        BlockPos pos = entity.blockPosition();
        playAt(level, pos, GCSounds.SWITCH, pitch);
        GCSchedulerUtil.runLater(level.getServer(), SECOND_SOUND_DELAY_TICKS,
                () -> playAt(level, pos, GCSounds.SWITCH, pitch));
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        return entity.getConsole() == null ? InteractionResult.FAIL : InteractionResult.SUCCESS;
    }
}
