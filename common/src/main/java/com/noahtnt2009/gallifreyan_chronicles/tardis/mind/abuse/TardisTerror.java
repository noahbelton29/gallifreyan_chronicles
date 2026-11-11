package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
import com.noahtnt2009.gallifreyan_chronicles.util.PlayerMotion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/* so traumatised it freezes up. only the mallet snaps it out of it, for a while */
public final class TardisTerror {
    private static final float TERROR_TRAUMA = 60f;
    private static final float TERROR_FEAR = 40f;
    private static final long COMPLY_TICKS = 600L;
    private static final float MAX_SAVAGE_DAMAGE = 10F;
    private static final float MIN_SAVAGE_DAMAGE = 4F;
    private static final int SAVAGE_BURN_SECONDS = 3;

    private static final Map<UUID, Episode> EPISODES = new HashMap<>();

    private static final class Episode {
        int required;
        int hits;
        long compliantUntil = Long.MIN_VALUE;
    }

    private TardisTerror() {
    }

    public static void reset() {
        EPISODES.clear();
    }

    public static boolean compliant(Tardis tardis) {
        Episode episode = EPISODES.get(tardis.id());
        return episode != null && TardisMindSystem.now(tardis.server()) < episode.compliantUntil;
    }

    public static boolean terrified(Tardis tardis) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        return mind.trauma() >= TERROR_TRAUMA && TardisMindSystem.fear(tardis) >= TERROR_FEAR && !compliant(tardis);
    }

    /* true if the hit was part of snapping it out of a freeze */
    public static boolean strike(Tardis tardis, ServerLevel level, Vec3 hit, Player player) {
        if (!terrified(tardis)) return false;
        TardisPersonality personality = TardisMindSystem.mind(tardis).personality();
        Episode episode = EPISODES.computeIfAbsent(tardis.id(), id -> new Episode());
        if (episode.required == 0) episode.required = hitsToComply(personality, level.getRandom());
        episode.hits++;

        if (personality == TardisPersonality.AGGRESSIVE) savage(level, hit, player, episode);

        if (episode.hits >= episode.required) {
            episode.compliantUntil = TardisMindSystem.now(tardis.server()) + COMPLY_TICKS;
            episode.hits = 0;
            episode.required = 0;
            Messages.overlay(player, ChatFormatting.GRAY, "tardis_terror_complies");
        } else if (personality != TardisPersonality.AGGRESSIVE) {
            Messages.overlay(player, ChatFormatting.DARK_GRAY, "tardis_terror_resists");
        }
        return true;
    }

    private static int hitsToComply(TardisPersonality personality, RandomSource random) {
        return switch (personality) {
            case OBEDIENT -> 1 + random.nextInt(2);
            case SUBMISSIVE -> 2 + random.nextInt(2);
            case DEFENSIVE -> 3 + random.nextInt(3);
            case AGGRESSIVE -> 6 + random.nextInt(5);
        };
    }

    /* fights for its life. weaker as it breaks */
    private static void savage(ServerLevel level, Vec3 hit, Player player, Episode episode) {
        float broken = episode.hits / (float) episode.required;
        float damage = Mth.lerp(broken, MAX_SAVAGE_DAMAGE, MIN_SAVAGE_DAMAGE);
        player.hurtServer(level, level.damageSources().lightningBolt(), damage);
        player.igniteForSeconds(SAVAGE_BURN_SECONDS);

        Vec3 away = player.position().subtract(hit).multiply(1, 0, 1);
        away = away.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : away.normalize();
        PlayerMotion.set(player, new Vec3(away.x * 1.4, 0.6, away.z * 1.4));

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1, player.getZ(), 40, 0.4, 0.6, 0.4, 0.5);
        level.sendParticles(ParticleTypes.FLAME, hit.x, hit.y, hit.z, 30, 0.5, 0.5, 0.5, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.7F, 1.6F);
        Messages.overlay(player, ChatFormatting.DARK_RED, "tardis_terror_savage");
    }
}
