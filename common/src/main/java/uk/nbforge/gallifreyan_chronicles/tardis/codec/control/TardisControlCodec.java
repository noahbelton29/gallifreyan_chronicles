package uk.nbforge.gallifreyan_chronicles.tardis.codec.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControlTypes;

import java.util.function.Function;

public final class TardisControlCodec {
    private static final Codec<MapCodec<? extends TardisControl>> TYPE_CODEC = Codec.STRING.comapFlatMap(
            id -> {
                MapCodec<? extends TardisControl> codec = TardisControlTypes.byId(id);
                return codec != null
                        ? DataResult.<MapCodec<? extends TardisControl>>success(codec)
                        : DataResult.<MapCodec<? extends TardisControl>>error(() -> "Unknown TARDIS control type: " + id);
            },
            codec -> {
                String id = TardisControlTypes.idOf(codec);
                if (id == null) {
                    throw new IllegalStateException("TARDIS control type is not registered: " + codec);
                }
                return id;
            }
    );

    public static final Codec<TardisControl> CODEC = TYPE_CODEC.dispatch("type", TardisControl::codec, Function.identity());

    private TardisControlCodec() {
    }
}
