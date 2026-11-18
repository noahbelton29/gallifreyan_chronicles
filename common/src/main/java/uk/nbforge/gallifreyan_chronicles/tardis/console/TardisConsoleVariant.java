package uk.nbforge.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.loader.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public record TardisConsoleVariant(
        String id, String parent, String texture, Map<String, String> glowTextures, int sortOrder)
        implements IdentifiableData<TardisConsoleVariant> {

    public static final Codec<TardisConsoleVariant> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(TardisConsoleVariant::id),
                    Codec.STRING.fieldOf("parent").forGetter(TardisConsoleVariant::parent),
                    Codec.STRING.fieldOf("texture").forGetter(TardisConsoleVariant::texture),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING)
                            .optionalFieldOf("glow_textures", Map.of())
                            .forGetter(TardisConsoleVariant::glowTextures),
                    Codec.INT
                            .optionalFieldOf("sort_order", TardisConsole.NO_SORT_ORDER)
                            .forGetter(TardisConsoleVariant::sortOrder))
            .apply(inst, TardisConsoleVariant::new));

    public static final StreamCodec<FriendlyByteBuf, TardisConsoleVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleVariant::id,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleVariant::parent,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleVariant::texture,
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
            TardisConsoleVariant::glowTextures,
            ByteBufCodecs.INT,
            TardisConsoleVariant::sortOrder,
            TardisConsoleVariant::new);

    public String localId() {
        return GCUtil.localPath(id);
    }

    public @Nullable String glowTexture(String groupName) {
        return glowTextures.get(groupName);
    }

    @Override
    public TardisConsoleVariant withId(String id) {
        return new TardisConsoleVariant(id, parent, texture, glowTextures, sortOrder);
    }
}
