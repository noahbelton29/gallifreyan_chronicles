package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface TardisControl {
    default InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    default boolean onLeftClick(TardisControlEntity entity, Player player) {
        return false;
    }

    default String getAnimation(boolean activated) {
        return null;
    }

    default void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
    }
}