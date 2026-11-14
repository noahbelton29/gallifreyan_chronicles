package uk.nbforge.gallifreyan_chronicles.tardis.mind.offering;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.FearLevel;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisBond;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
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
 * Handles gifts offered to a TARDIS as a way of repairing a damaged relationship.
 * <p>
 * Gifts can improve trust and reduce stress, fear, and trauma, but their
 * effectiveness decreases when offered repeatedly. They cannot replace
 * earning trust through normal interactions.
 */
public final class TardisPeaceOffering {
    private static final float TRUST_GAIN = 6f;
    private static final float STRESS_RELIEF = 10f;
    private static final float FEAR_RELIEF = 15f;
    private static final float TRAUMA_RELIEF = 1f;

    /**
     * Maximum trust level at which a peace offering can still be accepted.
     */
    private static final float TRUST_CEILING = 70f;

    /* scared or betrayed tardises still take gifts, just at half effect */
    private static final float WARY_EFFECT = 0.5f;

    /**
     * Multiplier applied to the effectiveness of gifts for TARDISes with PTSD.
     */
    private static final float PTSD_EFFECT = 0.5f;

    /**
     * Amount by which each consecutive gift reduces its effectiveness.
     */
    private static final float STREAK_FALLOFF = 0.25f;

    /**
     * Minimum effectiveness a gift can have, regardless of the streak.
     */
    private static final float MIN_EFFECT = 0.25f;

    /**
     * Minimum time between gifts from the same player to the same TARDIS.
     */
    private static final long COOLDOWN_TICKS = 6000L;

    /**
     * Time after which a previous gift no longer counts towards the streak.
     */
    private static final long STREAK_WINDOW_TICKS = 24000L;

    /**
     * Tracks the last offering made by each player to each TARDIS.
     */
    private static final Map<String, Offering> OFFERINGS = new HashMap<>();

    /**
     * Stores the timing and consecutive gift count for an offering.
     */
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
     * Attempts to offer a gift to a TARDIS.
     * <p>
     * A gift is rejected if the TARDIS has been betrayed, is too frightened,
     * already trusts the player sufficiently, or has recently received another
     * gift from them.
     * <p>
     * Accepted gifts improve the TARDIS's relationship with the player and
     * reduce its negative mental states. Repeated gifts have diminishing
     * effectiveness, and PTSD reduces their effectiveness further.
     *
     * @param tardis the TARDIS receiving the offering
     * @param level  the server level containing the offering
     * @param pos    the position where the offering is made
     * @param player the player making the offering
     * @return {@code true} if the TARDIS accepts the gift and it should be consumed
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

        // Gifts within the streak window become progressively less effective.
        int streak = last != null && now - last.tick < STREAK_WINDOW_TICKS ? last.streak + 1 : 0;
        float effect = Math.max(MIN_EFFECT, 1f - STREAK_FALLOFF * streak);

        boolean ptsd = mind.has(TardisDisability.PTSD);
        if (ptsd) effect *= 1f - (1f - PTSD_EFFECT) * mind.ptsdStrength();
        boolean betrayed = bond.betrayed();
        boolean afraid = TardisMindSystem.fearLevel(tardis, who).atLeast(FearLevel.AFRAID);
        if (betrayed || afraid) effect *= WARY_EFFECT;

        // Account for trust gain modifiers so the offering never pushes trust past the ceiling.
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