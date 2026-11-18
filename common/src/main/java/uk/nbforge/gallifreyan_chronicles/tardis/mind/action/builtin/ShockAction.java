package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.util.PlayerMotionUtil;

public record ShockAction(float range, float damage, float push, float lift) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":shock";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        Player player = context.player;
        if (context.level == null || context.pos == null || player == null) return;
        if (context.level != context.tardis.interiorLevel().orElse(null)) return;
        if (player.distanceToSqr(context.pos) > range * range) return;
        player.hurtServer(context.level, context.level.damageSources().lightningBolt(), damage);
        throwBack(player, context.pos, push, lift);
    }

    static void throwBack(Player player, Vec3 from, double push, double lift) {
        Vec3 away = player.position().subtract(from).multiply(1, 0, 1);
        away = away.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : away.normalize();
        PlayerMotionUtil.set(player, new Vec3(away.x * push, lift, away.z * push));
    }
}
