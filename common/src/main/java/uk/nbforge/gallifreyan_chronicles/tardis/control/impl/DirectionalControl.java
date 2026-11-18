package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;

public class DirectionalControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("directional_control_on", "directional_control_off", false);

    public DirectionalControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    protected boolean apply(Tardis tardis, @Nullable Player player) {
        int direction = player != null && player.isShiftKeyDown() ? -1 : 1;
        LandingTarget target = TardisDestinations.rotate(tardis, direction);
        if (target == null) return false;
        TardisDestinations.showDirection(player, target);
        return true;
    }
}
