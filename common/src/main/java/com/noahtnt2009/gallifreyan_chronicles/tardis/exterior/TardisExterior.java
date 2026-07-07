package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TardisExterior(String id) implements IdentifiableData<TardisExterior> {
    public static final Codec<TardisExterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(TardisExterior::id)
    ).apply(inst, TardisExterior::new));

    public static final StreamCodec<FriendlyByteBuf, TardisExterior> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisExterior::id,
            TardisExterior::new
    );

    @Override
    public TardisExterior withId(String id) {
        return new TardisExterior(id);
    }
}