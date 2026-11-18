package uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.FearLevel;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindActions;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;

public final class TardisTerror {
    private static final long COMPLY_TICKS = 600L;

    private static final Map<String, Episode> EPISODES = ServerState.track(new HashMap<>());

    private static final class Episode {
        int required;
        int hits;
        long compliantUntil = Long.MIN_VALUE;
    }

    private TardisTerror() {}

    public static void reset() {
        EPISODES.clear();
    }

    private static String key(Tardis tardis, UUID player) {
        return tardis.id() + "/" + player;
    }

    public static boolean compliant(Tardis tardis, @Nullable UUID player) {
        if (player == null) return false;
        Episode episode = EPISODES.get(key(tardis, player));
        return episode != null && TardisMinds.now(tardis.server()) < episode.compliantUntil;
    }

    public static boolean terrified(Tardis tardis, UUID player) {
        return TardisMinds.hasHurt(tardis, player)
                && TardisMinds.fearLevel(tardis, player) == FearLevel.TERRIFIED
                && !compliant(tardis, player);
    }

    /** True if the hit was part of snapping it out of a freeze. */
    public static boolean strike(Tardis tardis, ServerLevel level, Vec3 hit, Player player) {
        if (!terrified(tardis, player.getUUID())) return false;
        TardisPersonality personality = TardisMinds.mind(tardis).personality();
        Episode episode = EPISODES.computeIfAbsent(key(tardis, player.getUUID()), id -> new Episode());
        if (episode.required == 0)
            episode.required = Math.max(1, personality.terrorHits().pickInt(level.getRandom()));
        episode.hits++;

        MindContext context = new MindContext(tardis, level, player, hit);
        context.progress = episode.hits / (float) episode.required;
        MindActions.run(personality.onTerrorHit(), context);

        if (episode.hits >= episode.required) {
            episode.compliantUntil = TardisMinds.now(tardis.server()) + COMPLY_TICKS;
            episode.hits = 0;
            episode.required = 0;
            MindMessages.say(player, ChatFormatting.GRAY, "tardis_terror_complies");
        } else if (!context.handled) {
            MindMessages.say(player, ChatFormatting.DARK_GRAY, "tardis_terror_resists");
        }
        return true;
    }
}
