package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;

/* {"type": "particles", "particle": "minecraft:electric_spark", "count": 30,
    "spread": [0.6, 0.8, 0.6], "speed": 0.3, "height": 1} at the tardis */
public record ParticlesAction(SimpleParticleType particle, int count, Vec3 spread, float speed, float height)
        implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":particles";

    private static final Codec<SimpleParticleType> SIMPLE_PARTICLE = BuiltInRegistries.PARTICLE_TYPE.byNameCodec()
            .comapFlatMap(type -> type instanceof SimpleParticleType simple
                    ? DataResult.success(simple)
                    : DataResult.error(() -> "particle needs extra options, use a simple one"), simple -> simple);

    public static final MapCodec<ParticlesAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SIMPLE_PARTICLE.fieldOf("particle").forGetter(ParticlesAction::particle),
            Codec.INT.optionalFieldOf("count", 10).forGetter(ParticlesAction::count),
            Vec3.CODEC.optionalFieldOf("spread", new Vec3(0.5, 0.5, 0.5)).forGetter(ParticlesAction::spread),
            Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(ParticlesAction::speed),
            Codec.FLOAT.optionalFieldOf("height", 0f).forGetter(ParticlesAction::height)
    ).apply(inst, ParticlesAction::new));

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        if (context.level == null || context.pos == null) return;
        Vec3 at = context.pos;
        context.level.sendParticles(particle, at.x, at.y + height, at.z, count, spread.x, spread.y, spread.z, speed);
    }
}
