package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DirectionControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("direction_control", "direction_control", false);
    public static final MapCodec<DirectionControl> CODEC = codec(DEFAULT_ANIMATIONS, DirectionControl::new);

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

    @Override
    public MapCodec<DirectionControl> codec() { return CODEC; }
}
