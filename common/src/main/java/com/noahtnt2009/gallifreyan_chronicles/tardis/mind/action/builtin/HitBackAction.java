package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
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

/* {"type": "hit_back", "max_damage": 6, "min_damage": 2, "push": 0.9}
   for "on_mallet_hit". zaps whoever hit it, weaker the more scared it is. does nothing once it's cowed */
public record HitBackAction(float maxDamage, float minDamage, float push) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":hit_back";
    public static final MapCodec<HitBackAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("max_damage", 6f).forGetter(HitBackAction::maxDamage),
            Codec.FLOAT.optionalFieldOf("min_damage", 2f).forGetter(HitBackAction::minDamage),
            Codec.FLOAT.optionalFieldOf("push", 0.9f).forGetter(HitBackAction::push)
    ).apply(inst, HitBackAction::new));

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        ServerLevel level = context.level;
        Player player = context.player;
        if (level == null || player == null || context.pos == null) return;
        if (TardisMindSystem.cowed(context.tardis)) return;

        float toComply = Math.max(1f, TardisMindSystem.mind(context.tardis).personality().fearToComply());
        float scared = Mth.clamp(TardisMindSystem.fear(context.tardis) / toComply, 0f, 1f);
        player.hurtServer(level, level.damageSources().lightningBolt(), Mth.lerp(scared, maxDamage, minDamage));
        ShockAction.throwBack(player, context.pos, push, 0.4);

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1, player.getZ(),
                25, 0.3, 0.5, 0.3, 0.4);
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 0.8F, 1.4F);
        Messages.overlay(player, ChatFormatting.RED, "mallet_fights_back");
        context.handled = true;
    }
}
