package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record TardisInterior(String id, Identifier structure, Vec3 spawn, Direction facing,
                             Optional<Exit> exit, Optional<String> console, Optional<String> consoleVariant,
                             Optional<String> doorVariant)
        implements IdentifiableData<TardisInterior> {

    public record Exit(Vec3 min, Vec3 max) {
        public static final Codec<Exit> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                VEC3_CODEC.fieldOf("min").forGetter(Exit::min),
                VEC3_CODEC.fieldOf("max").forGetter(Exit::max)
        ).apply(inst, Exit::new));
    }

    private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
    ).apply(inst, Vec3::new));

    public static final Codec<TardisInterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisInterior::id),
            Identifier.CODEC.fieldOf("structure").forGetter(TardisInterior::structure),
            VEC3_CODEC.fieldOf("spawn").forGetter(TardisInterior::spawn),
            Direction.CODEC.optionalFieldOf("facing", Direction.SOUTH).forGetter(TardisInterior::facing),
            Exit.CODEC.optionalFieldOf("exit").forGetter(TardisInterior::exit),
            Codec.STRING.optionalFieldOf("console").forGetter(TardisInterior::console),
            Codec.STRING.optionalFieldOf("console_variant").forGetter(TardisInterior::consoleVariant),
            Codec.STRING.optionalFieldOf("door_variant").forGetter(TardisInterior::doorVariant)
    ).apply(inst, TardisInterior::new));

    public static final TardisInterior FALLBACK = new TardisInterior(
            "fallback", GCUtils.modIdentifier("template_interior"), new Vec3(7.4, 1.0, 3.5), Direction.SOUTH,
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public TardisInterior {
        if (facing.getAxis().isVertical()) {
            facing = Direction.SOUTH;
        }
    }

    public float spawnYaw() {
        return facing.toYRot();
    }

    public AABB exitBox() {
        if (exit.isPresent()) {
            Vec3 a = exit.get().min();
            Vec3 b = exit.get().max();
            return new AABB(a.x, a.y, a.z, b.x, b.y, b.z);
        }
        double fx = facing.getStepX();
        double fz = facing.getStepZ();
        double cx = spawn.x - fx;
        double cz = spawn.z - fz;
        double hx = fx != 0 ? 0.5 : 1.0;
        double hz = fz != 0 ? 0.5 : 1.0;
        return new AABB(cx - hx, spawn.y - 0.5, cz - hz, cx + hx, spawn.y + 2.0, cz + hz);
    }

    @Override
    public TardisInterior withId(String id) {
        return new TardisInterior(id, structure, spawn, facing, exit, console, consoleVariant, doorVariant);
    }
}