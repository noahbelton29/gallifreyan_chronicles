package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ChangeIncrementControl extends DestinationControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("change_increment", "change_increment", false);
    public static final MapCodec<ChangeIncrementControl> CODEC = codec(DEFAULT_ANIMATIONS, ChangeIncrementControl::new);

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

    @Override
    public MapCodec<ChangeIncrementControl> codec() {
        return CODEC;
    }
}
