package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.util.GCSparkUtil;

/**
 * Used for on_mallet_hit, it zaps whoever hit it and is weaker the more scared it is, and does nothing once it is cowed.
 * The json fields are max_damage, min_damage and push.
 */
public record HitBackAction(float maxDamage, float minDamage, float push) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":hit_back";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        ServerLevel level = context.level;
        Player player = context.player;
        if (level == null || player == null || context.pos == null) return;
        if (TardisMindSystem.cowed(context.tardis, player.getUUID())) return;

        float toComply =
                Math.max(1f, TardisMindSystem.mind(context.tardis).personality().fearToComply());
        float scared = Mth.clamp(TardisMindSystem.fear(context.tardis, player.getUUID()) / toComply, 0f, 1f);
        player.hurtServer(level, level.damageSources().lightningBolt(), Mth.lerp(scared, maxDamage, minDamage));
        ShockAction.throwBack(player, context.pos, push, 0.4);

        GCSparkUtil.big(level, player.position().add(0, 1, 0));
        MindMessages.say(player, ChatFormatting.RED, "mallet_fights_back");
        context.handled = true;
    }
}
