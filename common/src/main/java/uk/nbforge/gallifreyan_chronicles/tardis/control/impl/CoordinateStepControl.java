package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;

public class CoordinateStepControl extends DestinationControl {
    public CoordinateStepControl(ControlAnimations animations) {
        super(animations, ClickSound.NONE);
    }

    @Override
    protected boolean blockedByNavigationalLock() {
        return false;
    }

    @Override
    protected boolean apply(Tardis tardis, @Nullable Player player) {
        int increment = player != null && player.isShiftKeyDown()
                ? TardisDestinations.toggleNegative(tardis)
                : TardisDestinations.cycleIncrement(tardis);
        TardisDestinations.showIncrement(player, increment);
        return true;
    }

    @Override
    public boolean onLeftClick(TardisControlEntity entity, Player player) {
        ControlContext ctx = ControlContext.of(entity);
        Tardis tardis = ctx == null ? null : ctx.tardis().orElse(null);
        if (tardis == null) return false;
        int increment = TardisDestinations.cycleIncrementBack(tardis);
        TardisDestinations.showIncrement(player, increment);
        ctx.console().playControlAnimationReversed(entity.getControlId(), player);
        return true;
    }
}
