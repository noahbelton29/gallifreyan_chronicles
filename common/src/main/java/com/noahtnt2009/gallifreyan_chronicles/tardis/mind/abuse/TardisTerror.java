package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.FearLevel;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.MindMessages;
import org.jspecify.annotations.Nullable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisPersonality;
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

/* so scared of a player it freezes up for them. it wears off by itself as its fear fades
   (leave it alone a few minutes), or the mallet snaps it out of it for a while */
public final class TardisTerror {
    private static final long COMPLY_TICKS = 600L;

    private static final Map<String, Episode> EPISODES = new HashMap<>();

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

    private static String key(Tardis tardis, UUID player) {
        return tardis.id() + "/" + player;
    }

    /* this player snapped it out of a freeze recently, so it does what they say */
    public static boolean compliant(Tardis tardis, @Nullable UUID player) {
        if (player == null) return false;
        Episode episode = EPISODES.get(key(tardis, player));
        return episode != null && TardisMindSystem.now(tardis.server()) < episode.compliantUntil;
    }

    /* only freezes up around players who hurt it */
    public static boolean terrified(Tardis tardis, UUID player) {
        return TardisMindSystem.hasHurt(tardis, player)
                && TardisMindSystem.fearLevel(tardis, player) == FearLevel.TERRIFIED
                && !compliant(tardis, player);
    }

    /* true if the hit was part of snapping it out of a freeze */
    public static boolean strike(Tardis tardis, ServerLevel level, Vec3 hit, Player player) {
        if (!terrified(tardis, player.getUUID())) return false;
        TardisPersonality personality = TardisMindSystem.mind(tardis).personality();
        Episode episode = EPISODES.computeIfAbsent(key(tardis, player.getUUID()), id -> new Episode());
        if (episode.required == 0) episode.required = Math.max(1, personality.terrorHits().pickInt(level.getRandom()));
        episode.hits++;

        MindContext context = new MindContext(tardis, level, player, hit);
        context.progress = episode.hits / (float) episode.required;
        MindActions.run(personality.onTerrorHit(), context);

        if (episode.hits >= episode.required) {
            episode.compliantUntil = TardisMindSystem.now(tardis.server()) + COMPLY_TICKS;
            episode.hits = 0;
            episode.required = 0;
            MindMessages.say(player, ChatFormatting.GRAY, "tardis_terror_complies");
        } else if (!context.handled) {
            MindMessages.say(player, ChatFormatting.DARK_GRAY, "tardis_terror_resists");
        }
        return true;
    }


}
