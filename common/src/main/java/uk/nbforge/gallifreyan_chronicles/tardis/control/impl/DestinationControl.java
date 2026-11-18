package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;

public abstract class DestinationControl extends BaseControl {
    protected DestinationControl(ControlAnimations animations, ClickSound clickSound) {
        super(animations, clickSound);
    }

    protected DestinationControl(ControlAnimations animations) {
        this(animations, ClickSound.SWITCH);
    }

    protected abstract boolean apply(Tardis tardis, @Nullable Player player);

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        Tardis tardis = ctx.tardis().orElse(null);
        if (tardis == null || !apply(tardis, player)) {
            MessageUtil.overlay(player, ChatFormatting.RED, "no_destination_to_set");
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }
}
