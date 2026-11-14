package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindActions;

import java.util.List;

public final class MindActionCodec {
    public static final Codec<MindAction> CODEC = Codec.STRING.partialDispatch("type",
            action -> DataResult.success(action.type()),
            id -> {
                MapCodec<? extends MindAction> codec = MindActions.byType(id);
                return codec == null
                        ? DataResult.error(() -> "unknown tardis mind action " + id)
                        : DataResult.success(codec);
            });

    public static final Codec<List<MindAction>> LIST_CODEC = CODEC.listOf();

    private MindActionCodec() {
    }
}
