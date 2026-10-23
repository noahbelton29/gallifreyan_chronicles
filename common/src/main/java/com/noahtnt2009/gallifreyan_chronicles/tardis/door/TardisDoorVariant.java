package com.noahtnt2009.gallifreyan_chronicles.tardis.door;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TardisDoorVariant(String id, String parent, String texture, boolean isDefault)
        implements IdentifiableData<TardisDoorVariant> {

    public static final Codec<TardisDoorVariant> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisDoorVariant::id),
            Codec.STRING.fieldOf("parent").forGetter(TardisDoorVariant::parent),
            Codec.STRING.fieldOf("texture").forGetter(TardisDoorVariant::texture),
            Codec.BOOL.optionalFieldOf("default", false).forGetter(TardisDoorVariant::isDefault)
    ).apply(inst, TardisDoorVariant::new));

    public static final StreamCodec<FriendlyByteBuf, TardisDoorVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisDoorVariant::id,
            ByteBufCodecs.STRING_UTF8, TardisDoorVariant::parent,
            ByteBufCodecs.STRING_UTF8, TardisDoorVariant::texture,
            ByteBufCodecs.BOOL, TardisDoorVariant::isDefault,
            TardisDoorVariant::new
    );

    public String localId() {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    @Override
    public TardisDoorVariant withId(String id) {
        return new TardisDoorVariant(id, parent, texture, isDefault);
    }
}
