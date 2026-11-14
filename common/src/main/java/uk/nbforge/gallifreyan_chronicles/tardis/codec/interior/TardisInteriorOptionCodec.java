package uk.nbforge.gallifreyan_chronicles.tardis.codec.interior;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisInteriorOptions;

public final class TardisInteriorOptionCodec {
    public static final StreamCodec<ByteBuf, TardisInteriorOptions.Option> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisInteriorOptions.Option::interiorId,
            ByteBufCodecs.STRING_UTF8, TardisInteriorOptions.Option::exteriorId,
            TardisInteriorOptions.Option::new
    );

    private TardisInteriorOptionCodec() {
    }
}
