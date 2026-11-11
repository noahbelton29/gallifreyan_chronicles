package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.util.PlayerMotion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/* {"type": "shock", "range": 4, "damage": 2, "push": 0.6, "lift": 0.3}
   zaps the player if they're close enough and throws them back */
public record ShockAction(float range, float damage, float push, float lift) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":shock";
    public static final MapCodec<ShockAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("range", 4f).forGetter(ShockAction::range),
            Codec.FLOAT.optionalFieldOf("damage", 2f).forGetter(ShockAction::damage),
            Codec.FLOAT.optionalFieldOf("push", 0.6f).forGetter(ShockAction::push),
            Codec.FLOAT.optionalFieldOf("lift", 0.3f).forGetter(ShockAction::lift)
    ).apply(inst, ShockAction::new));

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        Player player = context.player;
        if (context.level == null || context.pos == null || player == null) return;
        if (player.distanceToSqr(context.pos) > range * range) return;
        player.hurtServer(context.level, context.level.damageSources().lightningBolt(), damage);
        throwBack(player, context.pos, push, lift);
    }

    static void throwBack(Player player, Vec3 from, double push, double lift) {
        Vec3 away = player.position().subtract(from).multiply(1, 0, 1);
        away = away.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : away.normalize();
        PlayerMotion.set(player, new Vec3(away.x * push, lift, away.z * push));
    }
}
