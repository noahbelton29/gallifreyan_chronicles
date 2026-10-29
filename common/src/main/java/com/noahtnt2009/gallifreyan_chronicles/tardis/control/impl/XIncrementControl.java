package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class XIncrementControl extends DestinationControl {
    public static final String ID = "x_increment";
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("x_increment", "x_increment", false);
    public static final MapCodec<XIncrementControl> CODEC = codec(DEFAULT_ANIMATIONS, XIncrementControl::new);

    public XIncrementControl() { this(DEFAULT_ANIMATIONS); }
    public XIncrementControl(ControlAnimations animations) { super(animations); }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        LandingTarget target = TardisDestinations.step(server, tardisId, TardisDestinations.Axis.X);
        if (target == null) return false;
        TardisDestinations.showDestination(player, TardisDestinations.Axis.X, target);
        return true;
    }

    @Override
    public MapCodec<XIncrementControl> codec() { return CODEC; }
}
