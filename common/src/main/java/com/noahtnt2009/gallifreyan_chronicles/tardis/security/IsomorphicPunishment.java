package com.noahtnt2009.gallifreyan_chronicles.tardis.security;

import com.noahtnt2009.gallifreyan_chronicles.block.ConsoleBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.joml.Math;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class IsomorphicPunishment {
    private static final float FIRST_DAMAGE = 3.0F;
    private static final float EXTRA_DAMAGE_PER_STRIKE = 2.0F;

    private static final int FIRST_BLINDNESS_TICKS = 100;
    private static final int EXTRA_EFFECT_TICKS_PER_STRIKE = 40;
    private static final int MAX_EFFECT_TICKS = 400;
    private static final int SLOWNESS_AMPLIFIER = 1;

    private static final long COUNT_COOLDOWN_TICKS = 20;
    private static final long FORGIVE_AFTER_TICKS = 12000;

    private record Record(int strikes, long lastGameTime, int deaths) {
    }

    private static final Map<UUID, Record> RECORDS = new HashMap<>();

    private IsomorphicPunishment() {
    }

    public static void punish(Player player, TardisConsoleBlockEntity console) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ServerLevel level = serverPlayer.level();

        long now = level.getGameTime();
        int deaths = serverPlayer.getStats().getValue(Stats.CUSTOM, Stats.DEATHS);
        Record old = RECORDS.get(player.getUUID());
        boolean forgiven = old == null
                || now - old.lastGameTime() > FORGIVE_AFTER_TICKS
                || deaths != old.deaths();
        int strikes = forgiven ? 0 : old.strikes();

        if (!forgiven && now - old.lastGameTime() < COUNT_COOLDOWN_TICKS) {
            return;
        }

        strikes++;
        RECORDS.put(player.getUUID(), new Record(strikes, now, deaths));

        serverPlayer.sendOverlayMessage(Component.translatable("message.gallifreyan_chronicles.isomorphic_denied")
                .withStyle(ChatFormatting.RED));

        float damage = FIRST_DAMAGE + EXTRA_DAMAGE_PER_STRIKE * (strikes - 1);
        serverPlayer.hurtServer(level, level.damageSources().magic(), damage);

        int blindness = Math.min(FIRST_BLINDNESS_TICKS + EXTRA_EFFECT_TICKS_PER_STRIKE * (strikes - 1), MAX_EFFECT_TICKS);
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindness, 0, false, false, true));

        if (strikes >= 2) {
            int slowness = Math.min(FIRST_BLINDNESS_TICKS + EXTRA_EFFECT_TICKS_PER_STRIKE * (strikes - 2), MAX_EFFECT_TICKS);
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, slowness, SLOWNESS_AMPLIFIER, false, false, true));
        }

        ConsoleBlock.repel(level, console.getBlockPos(), serverPlayer);
    }

    public static void reset() {
        RECORDS.clear();
    }
}
