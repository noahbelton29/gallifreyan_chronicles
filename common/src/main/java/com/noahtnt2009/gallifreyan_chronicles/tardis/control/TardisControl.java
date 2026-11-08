package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface TardisControl {
    MapCodec<? extends TardisControl> codec();

    default InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /* the sound this control makes when it's used. played for you after a
       successful right click (and on consoles outside a tardis, which only animate),
       so onRightClick doesn't need to play it */
    default void playClickSound(TardisControlEntity entity) {
    }

    default boolean onLeftClick(TardisControlEntity entity, Player player) {
        return false;
    }

    default String getAnimation(boolean activated) {
        return null;
    }

    default boolean snapsWhenInactive() {
        return false;
    }

    default void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
    }
}