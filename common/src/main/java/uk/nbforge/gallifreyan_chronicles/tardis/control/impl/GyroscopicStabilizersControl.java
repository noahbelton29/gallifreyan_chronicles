package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;

public class GyroscopicStabilizersControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("gyroscopic_stabilizers_on", "gyroscopic_stabilizers_off", true);

    public GyroscopicStabilizersControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        if (!ctx.state(GCTardisControls.GYROSCOPIC_STABILIZERS)) {
            playSound(entity, GCSounds.TARDIS_STABILIZERS_ON, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("stabilizers", "engaged", ChatFormatting.GREEN)
                : state("stabilizers", "disengaged", ChatFormatting.RED);
    }
}
