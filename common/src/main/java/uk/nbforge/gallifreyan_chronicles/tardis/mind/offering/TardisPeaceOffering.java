package uk.nbforge.gallifreyan_chronicles.tardis.mind.offering;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.FearLevel;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisBond;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
   Gifts to a TARDIS to repair the relationship.
   They can raise trust and lower stress, fear and trauma but work less when repeated, and they cannot replace earning trust normally.
*/
public final class TardisPeaceOffering {
    private static final float TRUST_GAIN = 6f;
    private static final float STRESS_RELIEF = 10f;
    private static final float FEAR_RELIEF = 15f;
    private static final float TRAUMA_RELIEF = 1f;

    // Above this trust a gift is no longer accepted
    private static final float TRUST_CEILING = 70f;

    // Scared or betrayed TARDISes still take gifts, just at half effect
    private static final float WARY_EFFECT = 0.5f;

    // Gifts are weaker for a TARDIS with PTSD
    private static final float PTSD_EFFECT = 0.5f;

    // How much weaker each gift in a row is
    private static final float STREAK_FALLOFF = 0.25f;

    // The weakest a gift can be, whatever the streak
    private static final float MIN_EFFECT = 0.25f;

    // Time between gifts from the same player to the same TARDIS
    private static final long COOLDOWN_TICKS = 6000L;

    // After this long a past gift no longer counts towards the streak
    private static final long STREAK_WINDOW_TICKS = 24000L;

    // The last gift from each player to each TARDIS
    private static final Map<String, Offering> OFFERINGS = new HashMap<>();

    /** When it was given and how many in a row. */
    private static final class Offering {
        final long tick;
        final int streak;

        Offering(long tick, int streak) {
            this.tick = tick;
            this.streak = streak;
        }
    }

    private TardisPeaceOffering() {
    }

    public static void reset() {
        OFFERINGS.clear();
    }

    /**
       A gift is rejected if it was betrayed, is too frightened, already trusts the player enough or got a gift from them recently.
       Accepted gifts improve the relationship and lower the bad states, repeats work less and PTSD lowers it more.
       Returns true if the gift was accepted and should be used up.
    */
    public static boolean offer(Tardis tardis, ServerLevel level, BlockPos pos, Player player) {
        UUID who = player.getUUID();
        TardisMind mind = TardisMindSystem.mind(tardis);
        TardisBond bond = TardisMindSystem.bond(tardis, who);

        if (bond.trust() >= TRUST_CEILING - 1f) {
            MindMessages.say(player, ChatFormatting.GRAY, "tardis_offering_content");
            return false;
        }

        long now = TardisMindSystem.now(tardis.server());
        String key = tardis.id() + "/" + who;
        Offering last = OFFERINGS.get(key);
        if (last != null && now - last.tick < COOLDOWN_TICKS) {
            MindMessages.say(player, ChatFormatting.GRAY, "tardis_offering_wait");
            return false;
        }

        // Gifts inside the streak window get weaker and weaker
        int streak = last != null && now - last.tick < STREAK_WINDOW_TICKS ? last.streak + 1 : 0;
        float effect = Math.max(MIN_EFFECT, 1f - STREAK_FALLOFF * streak);

        boolean ptsd = mind.has(TardisDisability.PTSD);
        if (ptsd) effect *= 1f - (1f - PTSD_EFFECT) * mind.ptsdStrength();
        boolean betrayed = bond.betrayed();
        boolean afraid = TardisMindSystem.fearLevel(tardis, who).atLeast(FearLevel.AFRAID);
        if (betrayed || afraid) effect *= WARY_EFFECT;

        // Takes the trust gain modifiers into account so a gift never pushes trust past the ceiling
        float scale = Math.max(0.01f, mind.modifiersFor(bond).trustGain());
        float room = (TRUST_CEILING - bond.trust()) / scale;
        float gain = Math.min(TRUST_GAIN * effect, room);

        TardisMindSystem.addTrust(tardis, who, gain);
        TardisMindSystem.addStress(tardis, -STRESS_RELIEF * effect);
        TardisMindSystem.addFear(tardis, who, -FEAR_RELIEF * effect);
        TardisMindSystem.setTrauma(tardis, Math.max(0f, TardisMindSystem.trauma(tardis) - TRAUMA_RELIEF * effect));
        TardisMindSystem.forgiveOneHit(tardis, who, TRAUMA_RELIEF);
        OFFERINGS.put(key, new Offering(now, streak));

        level.playSound(null, pos, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.BLOCKS, 0.8F, 1.2F);
        String message = betrayed ? "tardis_offering_betrayed" : afraid ? "tardis_offering_afraid"
                : ptsd ? "tardis_offering_nervous" : streak > 0 ? "tardis_offering_again" : "tardis_offering_accepted";
        MindMessages.say(player, ChatFormatting.LIGHT_PURPLE, message);
        return true;
    }
}