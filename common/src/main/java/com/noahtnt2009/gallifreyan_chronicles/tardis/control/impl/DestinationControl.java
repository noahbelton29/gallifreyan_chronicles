package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public abstract class DestinationControl extends BaseControl {
    protected DestinationControl(ControlAnimations animations) {
        super(animations);
    }

    protected abstract boolean apply(MinecraftServer server, UUID tardisId, Player player);

    protected boolean playsSound() {
        return true;
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        TardisConsoleBlockEntity console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;
        if (!(entity.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;

        UUID tardisId = tardisId(console, level);
        if (tardisId == null || !apply(level.getServer(), tardisId, player)) {
            tell(player, "This console has no TARDIS destination to set!");
            return InteractionResult.FAIL;
        }

        if (playsSound()) playSound(entity, GCSounds.SWITCH, 0.8f);
        return InteractionResult.SUCCESS;
    }
}
