package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ZIncrementControl extends DestinationControl {
    public static final String ID = "z_increment";
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("z_increment", "z_increment", false);
    public static final MapCodec<ZIncrementControl> CODEC = codec(DEFAULT_ANIMATIONS, ZIncrementControl::new);

    public ZIncrementControl() { this(DEFAULT_ANIMATIONS); }
    public ZIncrementControl(ControlAnimations animations) { super(animations); }

    @Override
    protected boolean apply(MinecraftServer server, UUID tardisId, Player player) {
        LandingTarget target = TardisDestinations.step(server, tardisId, TardisDestinations.Axis.Z);
        if (target == null) return false;
        TardisDestinations.showDestination(player, TardisDestinations.Axis.Z, target);
        return true;
    }

    @Override
    public MapCodec<ZIncrementControl> codec() { return CODEC; }
}
