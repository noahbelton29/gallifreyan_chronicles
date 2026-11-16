package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.util.GCSparkUtil;

/**
 * Used for on_terror_hit, it fights for its life and is weaker the closer it is to breaking.
 * The json fields are max_damage, min_damage and burn_seconds.
 */
public record SavageAction(float maxDamage, float minDamage, int burnSeconds) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":savage";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        ServerLevel level = context.level;
        Player player = context.player;
        Vec3 hit = context.pos;
        if (level == null || player == null || hit == null) return;

        player.hurtServer(
                level, level.damageSources().lightningBolt(), Mth.lerp(context.progress, maxDamage, minDamage));
        player.igniteForSeconds(burnSeconds);
        ShockAction.throwBack(player, hit, 1.4, 0.6);

        GCSparkUtil.blast(level, hit);
        GCSparkUtil.big(level, player.position().add(0, 1, 0));
        MindMessages.say(player, ChatFormatting.DARK_RED, "tardis_terror_savage");
        context.handled = true;
    }
}
