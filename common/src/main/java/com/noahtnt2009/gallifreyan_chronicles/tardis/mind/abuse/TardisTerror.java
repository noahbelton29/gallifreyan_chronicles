package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
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

    /* only freezes up around players who hurt it */
    public static boolean terrified(Tardis tardis, UUID player) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        return mind.trauma() >= TERROR_TRAUMA && TardisMindSystem.hasHurt(tardis, player)
                && TardisMindSystem.fear(tardis, player) >= TERROR_FEAR && !compliant(tardis);
    }

    /* true if the hit was part of snapping it out of a freeze */
    public static boolean strike(Tardis tardis, ServerLevel level, Vec3 hit, Player player) {
        if (!terrified(tardis, player.getUUID())) return false;
        TardisPersonality personality = TardisMindSystem.mind(tardis).personality();
        Episode episode = EPISODES.computeIfAbsent(tardis.id(), id -> new Episode());
        if (episode.required == 0) episode.required = Math.max(1, personality.terrorHits().pickInt(level.getRandom()));
        episode.hits++;

        MindContext context = new MindContext(tardis, level, player, hit);
        context.progress = episode.hits / (float) episode.required;
        MindActions.run(personality.onTerrorHit(), context);

        if (episode.hits >= episode.required) {
            episode.compliantUntil = TardisMindSystem.now(tardis.server()) + COMPLY_TICKS;
            episode.hits = 0;
            episode.required = 0;
            Messages.overlay(player, ChatFormatting.GRAY, "tardis_terror_complies");
        } else if (!context.handled) {
            Messages.overlay(player, ChatFormatting.DARK_GRAY, "tardis_terror_resists");
        }
        return true;
    }


}
