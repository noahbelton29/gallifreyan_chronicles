package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public record TardisConsoleVariant(String id, String parent, String texture, Map<String, String> glowTextures)
        implements IdentifiableData<TardisConsoleVariant> {

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

    public String localId() {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    public @Nullable String glowTexture(String groupName) {
        return glowTextures.get(groupName);
    }

    @Override
    public TardisConsoleVariant withId(String id) {
        return new TardisConsoleVariant(id, parent, texture, glowTextures);
    }
}
