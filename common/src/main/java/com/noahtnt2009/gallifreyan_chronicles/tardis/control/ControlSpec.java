package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record ControlSpec(String id, Vec3 offset, float width, float height, float depth,
                          float rotX, float rotY, float rotZ) {

    private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
    ).apply(inst, Vec3::new));

    private record Rotation(float x, float y, float z) {
        private static final Codec<Rotation> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.fieldOf("x").forGetter(Rotation::x),
                Codec.FLOAT.fieldOf("y").forGetter(Rotation::y),
                Codec.FLOAT.fieldOf("z").forGetter(Rotation::z)
        ).apply(inst, Rotation::new));
    }

    public static final Codec<ControlSpec> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(ControlSpec::id),
            VEC3_CODEC.fieldOf("offset").forGetter(ControlSpec::offset),
            Codec.FLOAT.fieldOf("width").forGetter(ControlSpec::width),
            Codec.FLOAT.fieldOf("height").forGetter(ControlSpec::height),
            Codec.FLOAT.optionalFieldOf("depth").forGetter(spec -> Optional.of(spec.depth)),
            Rotation.CODEC.optionalFieldOf("rotation").forGetter(spec ->
                    Optional.of(new Rotation(spec.rotX, spec.rotY, spec.rotZ)))
    ).apply(inst, (id, offset, width, height, depth, rotation) -> {
        Rotation rot = rotation.orElse(new Rotation(0f, 0f, 0f));
        return new ControlSpec(id, offset, width, height, depth.orElse(width), rot.x(), rot.y(), rot.z());
    }));

    public ControlSpec(String id, Vec3 offset, float width, float height) {
        this(id, offset, width, height, width, 0f, 0f, 0f);
    }

    public ControlSpec(String id, Vec3 offset, float width, float height, float depth) {
        this(id, offset, width, height, depth, 0f, 0f, 0f);
    }
}