package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ChangeIncrementControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("change_increment", "change_increment", false);
    public static final MapCodec<ChangeIncrementControl> CODEC = codec(DEFAULT_ANIMATIONS, ChangeIncrementControl::new);

    public ChangeIncrementControl() { this(DEFAULT_ANIMATIONS); }
    public ChangeIncrementControl(ControlAnimations animations) { super(animations); }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        int increment = player != null && player.isShiftKeyDown()
                ? TardisDestinations.toggleNegative(server, tardisId)
                : TardisDestinations.cycleIncrement(server, tardisId);
        TardisDestinations.showIncrement(player, increment);
        return true;
    }

    @Override
    protected boolean playsSound() { return false; }

    @Override
    public MapCodec<ChangeIncrementControl> codec() { return CODEC; }
}
