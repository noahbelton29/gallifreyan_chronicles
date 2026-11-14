package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* {"type": "shield", "radius": 2.5, "push": 0.6}
   it throws up a glowing shield and gently pushes away anyone too close. no damage */
public record ShieldAction(float radius, float push, float lift) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":shield";
    private static final int DOTS = 80;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.everywhere(this::raise);
    }

    private void raise(ServerLevel level, Vec3 centre) {
        level.playSound(null, BlockPos.containing(centre), SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.8F, 1.4F);

        /* dots spread evenly over a dome */
        for (int i = 0; i < DOTS; i++) {
            double up = (i + 0.5) / DOTS;
            double around = i * 2.39996;
            double flat = Math.sqrt(1 - up * up);
            double x = centre.x + Math.cos(around) * flat * radius;
            double y = centre.y - 0.5 + up * radius;
            double z = centre.z + Math.sin(around) * flat * radius;
            level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0);
        }

        AABB area = new AABB(centre, centre).inflate(radius);
        for (Player player : level.getEntitiesOfClass(Player.class, area)) {
            ShockAction.throwBack(player, centre, push, lift);
        }
    }
}
