package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * A registered TARDIS interior, loaded from {@code data/<ns>/tardis_interior/<name>.json}.
 * <p>
 * All positions are relative to the origin the structure is placed at.
 *
 * @param structure structure NBT template id (data/&lt;ns&gt;/structure/&lt;path&gt;.nbt)
 * @param spawn     where a player is teleported to when entering
 * @param facing    horizontal direction the player faces on arrival
 * @param exit      optional exit trigger volume; derived from spawn + facing when absent
 * @param exteriors exterior ids that use this interior
 */
public record TardisInterior(String id, Identifier structure, Vec3 spawn, Direction facing,
                             Optional<Exit> exit, List<String> exteriors)
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
            Codec.STRING.listOf().optionalFieldOf("exteriors", List.of()).forGetter(TardisInterior::exteriors)
    ).apply(inst, TardisInterior::new));

    /** Used only if no interior JSON has been loaded at all. */
    public static final TardisInterior FALLBACK = new TardisInterior(
            "fallback", GCUtils.of("template_interior"), new Vec3(7.4, 1.0, 3.5), Direction.SOUTH,
            Optional.empty(), List.of());

    public TardisInterior {
        if (facing.getAxis().isVertical()) {
            facing = Direction.SOUTH;
        }
    }

    public float spawnYaw() {
        return facing.toYRot();
    }

    /** Exit trigger volume relative to the origin. By default a 2x1 box 0.5-1.5 blocks behind the spawn. */
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
        // half extents: 0.5 along facing, 1.0 across it
        double hx = fx != 0 ? 0.5 : 1.0;
        double hz = fz != 0 ? 0.5 : 1.0;
        return new AABB(cx - hx, spawn.y - 0.5, cz - hz, cx + hx, spawn.y + 2.0, cz + hz);
    }

    @Override
    public TardisInterior withId(String id) {
        return new TardisInterior(id, structure, spawn, facing, exit, exteriors);
    }
}
