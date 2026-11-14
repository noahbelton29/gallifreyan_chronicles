package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ChangeIncrementControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("change_increment", "change_increment", false);

    public ChangeIncrementControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public ChangeIncrementControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        int increment = player != null && player.isShiftKeyDown()
                ? TardisDestinations.toggleNegative(server, tardisId)
                : TardisDestinations.cycleIncrement(server, tardisId);
        TardisDestinations.showIncrement(player, increment);
        return true;
    }

    @Override
    public boolean onLeftClick(TardisControlEntity entity, Player player) {
        TardisConsoleBlockEntity console = entity.getConsole();
        if (console == null || !(entity.level() instanceof ServerLevel level)) return false;
        UUID tardisId = linkedTardis(console).map(Tardis::id).orElse(null);
        if (tardisId == null) return false;
        int increment = TardisDestinations.cycleIncrementBack(level.getServer(), tardisId);
        TardisDestinations.showIncrement(player, increment);
        console.playControlAnimationReversed(entity.getControlId(), player);
        return true;
    }

    @Override
    protected boolean playsSound() {
        return false;
    }
}
