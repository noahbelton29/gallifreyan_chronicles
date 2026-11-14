package uk.nbforge.gallifreyan_chronicles.tardis.codec.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.codec.Vec3Codec;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlSpec;

public final class ControlSpecCodec {
    private static final Codec<Vec3> VEC3_CODEC = Vec3Codec.CODEC;

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
            Codec.FLOAT.optionalFieldOf("depth").forGetter(spec -> Optional.of(spec.depth())),
            Rotation.CODEC.optionalFieldOf("rotation").forGetter(spec ->
                    Optional.of(new Rotation(spec.rotX(), spec.rotY(), spec.rotZ())))
    ).apply(inst, (id, offset, width, height, depth, rotation) -> {
        Rotation rot = rotation.orElse(new Rotation(0f, 0f, 0f));
        return new ControlSpec(id, offset, width, height, depth.orElse(width), rot.x(), rot.y(), rot.z());
    }));

    private ControlSpecCodec() {
    }
}
