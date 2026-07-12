package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record ControlSpec(String id, Vec3 offset, float width, float height, float depth) {
    private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
    ).apply(inst, Vec3::new));

    public static final Codec<ControlSpec> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(ControlSpec::id),
            VEC3_CODEC.fieldOf("offset").forGetter(ControlSpec::offset),
            Codec.FLOAT.fieldOf("width").forGetter(ControlSpec::width),
            Codec.FLOAT.fieldOf("height").forGetter(ControlSpec::height),
            Codec.FLOAT.optionalFieldOf("depth").forGetter(spec -> Optional.of(spec.depth))
    ).apply(inst, (id, offset, width, height, depth) ->
            new ControlSpec(id, offset, width, height, depth.orElse(width))));

    public ControlSpec(String id, Vec3 offset, float width, float height) {
        this(id, offset, width, height, width);
    }
}