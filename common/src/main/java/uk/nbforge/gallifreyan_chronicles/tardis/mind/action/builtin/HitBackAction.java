package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.util.GCSparkUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/* {"type": "hit_back", "max_damage": 6, "min_damage": 2, "push": 0.9}
   for "on_mallet_hit". zaps whoever hit it, weaker the more scared it is. does nothing once it's cowed */
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

        float toComply = Math.max(1f, TardisMindSystem.mind(context.tardis).personality().fearToComply());
        float scared = Mth.clamp(TardisMindSystem.fear(context.tardis, player.getUUID()) / toComply, 0f, 1f);
        player.hurtServer(level, level.damageSources().lightningBolt(), Mth.lerp(scared, maxDamage, minDamage));
        ShockAction.throwBack(player, context.pos, push, 0.4);

        GCSparkUtil.big(level, player.position().add(0, 1, 0));
        MindMessages.say(player, ChatFormatting.RED, "mallet_fights_back");
        context.handled = true;
    }
}
