package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.Control;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;

public class CloisterBellControl extends Control {
    public CloisterBellControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        if (lockedWhileCrashing(ctx, player, "cloister_bell_locked_crashing")) return InteractionResult.FAIL;
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void changed(ControlContext ctx, boolean activated) {
        ctx.tardis().ifPresent(tardis -> {
            if (activated) {
                TardisAlarm.controlOn(tardis);
            } else {
                TardisAlarm.controlOff(tardis);
            }
        });
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("cloister_bell", "on", ChatFormatting.GREEN)
                : state("cloister_bell", "off", ChatFormatting.RED);
    }
}
