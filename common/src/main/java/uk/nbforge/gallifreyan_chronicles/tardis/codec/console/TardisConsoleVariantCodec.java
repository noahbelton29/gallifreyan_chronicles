package uk.nbforge.gallifreyan_chronicles.tardis.codec.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;

public final class TardisConsoleVariantCodec {
    public static final Codec<TardisConsoleVariant> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisConsoleVariant::id),
            Codec.STRING.fieldOf("parent").forGetter(TardisConsoleVariant::parent),
            Codec.STRING.fieldOf("texture").forGetter(TardisConsoleVariant::texture),
            Codec.unboundedMap(Codec.STRING, Codec.STRING)
                    .optionalFieldOf("glow_textures", Map.of()).forGetter(TardisConsoleVariant::glowTextures)
    ).apply(inst, TardisConsoleVariant::new));

    public static final StreamCodec<FriendlyByteBuf, TardisConsoleVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisConsoleVariant::id,
            ByteBufCodecs.STRING_UTF8, TardisConsoleVariant::parent,
            ByteBufCodecs.STRING_UTF8, TardisConsoleVariant::texture,
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), TardisConsoleVariant::glowTextures,
            TardisConsoleVariant::new
    );

    private TardisConsoleVariantCodec() {
    }
}
