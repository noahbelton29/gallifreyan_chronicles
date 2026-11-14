package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DimensionSelectorControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("dimension_selector", "dimension_selector", false);

    public DimensionSelectorControl() { this(DEFAULT_ANIMATIONS); }
    public DimensionSelectorControl(ControlAnimations animations) { super(animations); }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        LandingTarget target = player != null && player.isShiftKeyDown()
                ? TardisDestinations.previousDimension(server, tardisId)
                : TardisDestinations.nextDimension(server, tardisId);
        if (target == null) return false;
        TardisDestinations.showDimension(player, server, target);
        return true;
    }

    @Override
    public String getAnimation(boolean activated) { return null; }

    @Override
    protected boolean playsSound() { return false; }
}
