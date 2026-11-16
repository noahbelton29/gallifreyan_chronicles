package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;

public class GyroscopicStabilizersControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("gyroscopic_stabilizers_on", "gyroscopic_stabilizers_off", true);

    public GyroscopicStabilizersControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;
        boolean stabilizersOn = console.getControlState(GCTardisControls.GYROSCOPIC_STABILIZERS);
        if (!stabilizersOn) {
            playSound(entity, GCSounds.TARDIS_STABILIZERS_ON, 0.0f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        playSound(entity, GCSounds.SWITCH, 0.8f);
    }
}
