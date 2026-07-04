package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record TardisExterior(String id, Identifier model, Identifier texture, Identifier animation) {
    public static final Codec<TardisExterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("model").forGetter(TardisExterior::model),
            Identifier.CODEC.fieldOf("texture").forGetter(TardisExterior::texture),
            Identifier.CODEC.fieldOf("animation").forGetter(TardisExterior::animation)
    ).apply(inst, (model, texture, animation) ->
            new TardisExterior("__unset__", model, texture, animation)
    ));

    public static final StreamCodec<FriendlyByteBuf, TardisExterior> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TardisExterior::id,
            Identifier.STREAM_CODEC, TardisExterior::model,
            Identifier.STREAM_CODEC, TardisExterior::texture,
            Identifier.STREAM_CODEC, TardisExterior::animation,
            TardisExterior::new
    );

    public TardisExterior withId(String id) {
        return new TardisExterior(id, model, texture, animation);
    }
}
