package uk.nbforge.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface TardisControl {
    MapCodec<? extends TardisControl> codec();

    default InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
       The sound this control makes when it is used.
       It is played for you after a successful right click, and on consoles outside a TARDIS which only animate, so onRightClick does not need to play it.
    */
    default void playClickSound(TardisControlEntity entity) {
    }

    default boolean onLeftClick(TardisControlEntity entity, Player player) {
        return false;
    }

    default @Nullable Component describeState(boolean activated) {
        return null;
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