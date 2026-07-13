package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record TardisExterior(String id, Vec3 keyOffset) implements IdentifiableData<TardisExterior> {
    public static final Vec3 DEFAULT_KEY_OFFSET = new Vec3(0.0, -0.925, 1.0);

    private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
    ).apply(inst, Vec3::new));

    public static final Codec<TardisExterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(TardisExterior::id),
            VEC3_CODEC.optionalFieldOf("key_offset").forGetter(exterior -> Optional.of(exterior.keyOffset()))
    ).apply(inst, (id, keyOffset) -> new TardisExterior(id, keyOffset.orElse(DEFAULT_KEY_OFFSET))));

    public static final StreamCodec<FriendlyByteBuf, TardisExterior> STREAM_CODEC = StreamCodec.of(
            (buf, exterior) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, exterior.id());
                buf.writeDouble(exterior.keyOffset().x());
                buf.writeDouble(exterior.keyOffset().y());
                buf.writeDouble(exterior.keyOffset().z());
            },
            (buf) -> {
                String id = ByteBufCodecs.STRING_UTF8.decode(buf);
                Vec3 offset = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
                return new TardisExterior(id, offset);
            }
    );

    public TardisExterior(String id) {
        this(id, DEFAULT_KEY_OFFSET);
    }

    public Vec3 worldKeyOffset(float yawDegrees) {
        double radians = Math.toRadians(180.0 - yawDegrees);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double x = keyOffset.x() * cos + keyOffset.z() * sin;
        double z = -keyOffset.x() * sin + keyOffset.z() * cos;

        return new Vec3(x, keyOffset.y(), z);
    }

    @Override
    public TardisExterior withId(String id) {
        return new TardisExterior(id, keyOffset);
    }
}