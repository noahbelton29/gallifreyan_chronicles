package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface TardisControl {
    /**
     * The codec for this kind of control's settings. Must be registered in {@link TardisControlTypes}, which is how a
     * control is saved and read as {@code { "type": ..., <its fields> }}.
     */
    MapCodec<? extends TardisControl> codec();

    default InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
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