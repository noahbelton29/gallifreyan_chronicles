package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/* tardis mind messages above the hotbar. one every few seconds per player so they don't
   talk over each other. the important ones can always cut in */
public final class MindMessages {
    private static final long GAP_TICKS = 60L;
    private static final Set<String> IMPORTANT = Set.of(
            "tardis_betrayed", "tardis_forgives_you", "tardis_terror_complies", "tardis_terror_savage",
            "mallet_fights_back", "mallet_not_yours", "tardis_ptsd_easing");
    private static final Map<UUID, Long> LAST = new HashMap<>();

    private MindMessages() {
    }

    public static void reset() {
        LAST.clear();
    }

    public static void say(Player player, ChatFormatting colour, String key, Object... args) {
        long now = player.level().getGameTime();
        Long last = LAST.get(player.getUUID());
        boolean tooSoon = last != null && now - last < GAP_TICKS;
        if (tooSoon && !IMPORTANT.contains(key)) return;
        LAST.put(player.getUUID(), now);
        MessageUtil.overlay(player, colour, key, args);
    }
}
