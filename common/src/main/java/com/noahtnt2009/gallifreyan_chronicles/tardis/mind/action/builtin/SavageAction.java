package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.noahtnt2009.gallifreyan_chronicles.util.GCSparks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.MindMessages;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/* {"type": "savage", "max_damage": 10, "min_damage": 4, "burn_seconds": 3}
   for "on_terror_hit". fights for its life, weaker the closer it is to breaking */
public record SavageAction(float maxDamage, float minDamage, int burnSeconds) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":savage";
    public static final MapCodec<SavageAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("max_damage", 10f).forGetter(SavageAction::maxDamage),
            Codec.FLOAT.optionalFieldOf("min_damage", 4f).forGetter(SavageAction::minDamage),
            Codec.INT.optionalFieldOf("burn_seconds", 3).forGetter(SavageAction::burnSeconds)
    ).apply(inst, SavageAction::new));

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

        player.hurtServer(level, level.damageSources().lightningBolt(), Mth.lerp(context.progress, maxDamage, minDamage));
        player.igniteForSeconds(burnSeconds);
        ShockAction.throwBack(player, hit, 1.4, 0.6);

        GCSparks.blast(level, hit);
        GCSparks.big(level, player.position().add(0, 1, 0));
        MindMessages.say(player, ChatFormatting.DARK_RED, "tardis_terror_savage");
        context.handled = true;
    }
}
