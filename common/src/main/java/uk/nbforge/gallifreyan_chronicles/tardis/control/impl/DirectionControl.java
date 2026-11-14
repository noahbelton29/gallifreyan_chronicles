package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DirectionControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("direction_control_on", "direction_control_off", false);

    public DirectionControl() { this(DEFAULT_ANIMATIONS); }
    public DirectionControl(ControlAnimations animations) { super(animations); }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        int direction = player != null && player.isShiftKeyDown() ? -1 : 1;
        LandingTarget target = TardisDestinations.rotate(server, tardisId, direction);
        if (target == null) return false;
        TardisDestinations.showDirection(player, target);
        return true;
    }
}
