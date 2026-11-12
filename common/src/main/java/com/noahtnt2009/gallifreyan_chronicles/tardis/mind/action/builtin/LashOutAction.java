package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
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

    public static final MapCodec<LashOutAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("from_severity", 0.35f).forGetter(LashOutAction::fromSeverity),
            Codec.FLOAT.optionalFieldOf("min_damage", 1f).forGetter(LashOutAction::minDamage),
            Codec.FLOAT.optionalFieldOf("max_damage", 4f).forGetter(LashOutAction::maxDamage),
            Codec.FLOAT.optionalFieldOf("push", 0.7f).forGetter(LashOutAction::push),
            Codec.FLOAT.optionalFieldOf("lift", 0.3f).forGetter(LashOutAction::lift)
    ).apply(inst, LashOutAction::new));

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

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1, player.getZ(),
                20, 0.3, 0.5, 0.3, 0.3);
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 0.7F, 1.4F);
        Messages.overlay(player, ChatFormatting.RED, "tardis_lashes_out");
        context.handled = true;
    }
}
