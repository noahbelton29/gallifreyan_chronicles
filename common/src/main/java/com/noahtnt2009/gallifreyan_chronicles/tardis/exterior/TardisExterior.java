package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record TardisExterior(String id, Identifier model, Identifier texture, Identifier animation) {
    public static final Codec<TardisExterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("model").forGetter(TardisExterior::model),
            Identifier.CODEC.fieldOf("texture").forGetter(TardisExterior::texture),
            Identifier.CODEC.fieldOf("animation").forGetter(TardisExterior::animation)
    ).apply(inst, (model, texture, animation) ->
            new TardisExterior("__unset__", model, texture, animation)
    ));

    public TardisExterior withId(String id) {
        return new TardisExterior(id, model, texture, animation);
    }
}
