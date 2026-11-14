package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.util.GCSparkUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/* {"type": "lash_out", "from_severity": 0.35, "min_damage": 2, "max_damage": 6, "push": 0.9, "lift": 0.35}
   for "when_you_enter". a tardis that fears you zaps you and throws you back. nothing happens below
   from_severity, and the damage grows with how scared it is. it can hurt but never kill: it always
   leaves the player at least a little health */
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

        /* never lethal, whatever the numbers in the json say */
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
