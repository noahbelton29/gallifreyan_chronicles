package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/**
 * Data-driven TARDIS exterior definition.
 *
 * Loaded from {@code data/<namespace>/tardis_exterior/<id>.json} at world load via
 * {@link TardisExteriorLoader}, so datapacks can add or override entries without
 * touching mod code.
 *
 * JSON shape:
 * <pre>{@code
 * {
 *   "model":     "gallifreyan_chronicles:1963_police_box",
 *   "texture":   "gallifreyan_chronicles:textures/block/1963_police_box.png",
 *   "animation": "gallifreyan_chronicles:tardis_door/1963_police_box"
 * }
 * }</pre>
 *
 * The {@code id} field is derived from the JSON file path, not stored inside the file.
 */
public record TardisExterior(String id, Identifier model, Identifier texture, Identifier animation) {

    // Codec used by datagen and the loader — id is injected separately from the file path.
    public static final Codec<TardisExterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("model").forGetter(TardisExterior::model),
            Identifier.CODEC.fieldOf("texture").forGetter(TardisExterior::texture),
            Identifier.CODEC.fieldOf("animation").forGetter(TardisExterior::animation)
    ).apply(inst, (model, texture, animation) ->
            // id is a placeholder here; the loader replaces it with the resource path
            new TardisExterior("__unset__", model, texture, animation)
    ));

    /** Reconstruct with the correct id after decoding. */
    public TardisExterior withId(String id) {
        return new TardisExterior(id, model, texture, animation);
    }
}
