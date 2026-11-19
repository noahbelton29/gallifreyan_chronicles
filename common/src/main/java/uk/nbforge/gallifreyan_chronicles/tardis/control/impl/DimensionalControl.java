package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;

public class DimensionalControl extends DestinationControl {
    public DimensionalControl(ControlAnimations animations) {
        super(animations, ClickSound.NONE);
    }

    @Override
    protected boolean apply(Tardis tardis, @Nullable Player player) {
        LandingTarget target = player != null && player.isShiftKeyDown()
                ? TardisDestinations.previousDimension(tardis)
                : TardisDestinations.nextDimension(tardis);
        if (target == null) return false;
        TardisDestinations.showDimension(player, tardis.server(), target);
        return true;
    }

    @Override
    public @Nullable String getAnimation(boolean activated) {
        return null;
    }
}
