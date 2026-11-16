package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.util.GCSparkUtil;

/**
 * Used for when_you_enter, a TARDIS that fears you zaps you and throws you back.
 * Nothing happens below from_severity and the damage grows with how scared it is.
 * It can hurt but never kill, it always leaves the player at least a little health.
 * The json fields are from_severity, min_damage, max_damage, push and lift.
 */
public record LashOutAction(float fromSeverity, float minDamage, float maxDamage, float push, float lift)
        implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":lash_out";
    private static final float LEAVES_HEALTH = 1f;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        ServerLevel level = context.level;
        Player player = context.player;
        if (level == null || player == null || context.pos == null) return;
        if (context.severity < fromSeverity) return;

        float span = Math.max(0.01f, 1f - fromSeverity);
        float scale = Mth.clamp((context.severity - fromSeverity) / span, 0f, 1f);
        float damage = Mth.lerp(scale, minDamage, maxDamage);

        // Never lethal, whatever the numbers in the json say
        damage = Math.min(damage, player.getHealth() - LEAVES_HEALTH);
        if (damage > 0f) {
            player.hurtServer(level, level.damageSources().lightningBolt(), damage);
        }
        ShockAction.throwBack(player, context.pos, push, lift);

        GCSparkUtil.big(level, player.position().add(0, 1, 0));
        MindMessages.say(player, ChatFormatting.RED, "tardis_lashes_out");
        context.handled = true;
    }
}
