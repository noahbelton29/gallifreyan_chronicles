package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;

public class XIncrementControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("x_increment", "x_increment", false);

    public XIncrementControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        LandingTarget target = TardisDestinations.step(server, tardisId, TardisDestinations.Axis.X);
        if (target == null) return false;
        TardisDestinations.showDestination(player, TardisDestinations.Axis.X, target);
        return true;
    }
}
