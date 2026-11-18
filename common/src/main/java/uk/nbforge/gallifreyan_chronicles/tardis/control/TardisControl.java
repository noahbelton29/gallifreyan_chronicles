package uk.nbforge.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;

public interface TardisControl {

    static final Codec<MapCodec<? extends TardisControl>> TYPE_CODEC = Codec.STRING.comapFlatMap(
            id -> {
                MapCodec<? extends TardisControl> codec = TardisControls.typeCodec(id);
                return codec != null
                        ? DataResult.<MapCodec<? extends TardisControl>>success(codec)
                        : DataResult.<MapCodec<? extends TardisControl>>error(
                                () -> "Unknown TARDIS control type: " + id);
            },
            codec -> {
                String id = TardisControls.typeId(codec);
                if (id == null) {
                    throw new IllegalStateException("TARDIS control type is not registered: " + codec);
                }
                return id;
            });

    public static final Codec<TardisControl> CODEC =
            TYPE_CODEC.dispatch("type", TardisControl::codec, Function.identity());

    MapCodec<? extends TardisControl> codec();

    default InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /**
     * The sound this control makes when it is used.
     * It is played for you after a successful right click, and on consoles outside a TARDIS which only animate, so onRightClick does not need to play it.
     */
    default void playClickSound(TardisControlEntity entity) {}

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

    default void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {}
}
