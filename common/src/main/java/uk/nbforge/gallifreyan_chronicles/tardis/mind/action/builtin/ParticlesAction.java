package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;

/* {"type": "particles", "particle": "minecraft:electric_spark", "count": 30,
    "spread": [0.6, 0.8, 0.6], "speed": 0.3, "height": 1} at the tardis */
public record ParticlesAction(SimpleParticleType particle, int count, Vec3 spread, float speed, float height)
        implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":particles";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.here((level, at) ->
                level.sendParticles(particle, at.x, at.y + height, at.z, count, spread.x, spread.y, spread.z, speed));
    }
}
