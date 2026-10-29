package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class YIncrementControl extends DestinationControl {
    public static final String ID = "y_increment";
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("y_increment", "y_increment", false);
    public static final MapCodec<YIncrementControl> CODEC = codec(DEFAULT_ANIMATIONS, YIncrementControl::new);

    public YIncrementControl() { this(DEFAULT_ANIMATIONS); }
    public YIncrementControl(ControlAnimations animations) { super(animations); }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        LandingTarget target = TardisDestinations.step(server, tardisId, TardisDestinations.Axis.Y);
        if (target == null) return false;
        TardisDestinations.showDestination(player, TardisDestinations.Axis.Y, target);
        return true;
    }

    @Override
    public MapCodec<YIncrementControl> codec() { return CODEC; }
}
