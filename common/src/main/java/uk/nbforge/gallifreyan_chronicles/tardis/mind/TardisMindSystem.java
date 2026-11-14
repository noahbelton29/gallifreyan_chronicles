package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.flight.TardisTurbulence;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisMallet;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisTrauma;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDyslexia;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.feedback.TardisFeedback;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.fright.TardisFright;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.offering.TardisPeaceOffering;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisMoods;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal.TardisAnger;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal.TardisConvincing;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindActions;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

/**
 * Manages the TARDIS's mind, including its emotions, personality, relationships and behaviour.
 *
 * <p>Tracks things such as stress, fear and trust, and decides how the TARDIS
 * responds to its pilot based on its personality and traits.</p>
 */
public final class TardisMindSystem {

    /**
     * Stress added when a frightened TARDIS is forced to cooperate.
     */
    private static final float COWED_STRESS = 4f;
    private static final float GENTLE_USE_TRUST = 1f;
    private static final long GENTLE_USE_COOLDOWN_TICKS = 1200L;
    private static final Map<String, Long> LAST_GENTLE_USE = new HashMap<>();

    /**
     * Trust lost when a frightened TARDIS is forced to cooperate.
     */
    /* forcing it with fear works, but it costs. kindness should always be the better way */
    private static final float COWED_TRUST_LOSS = 4f;
    private static final float COWED_TRAUMA = 1f;

    /**
     * Trust required for the TARDIS to be considered devoted to its owner.
     */
    private static final float DEVOTED_TRUST = 75f;

    /**
     * Stress required before the TARDIS reveals that strangers upset it.
     */
    private static final float STRANGER_REVEAL_STRESS = 25f;

    /**
     * Multiplier applied to positive stress gains when the TARDIS has PTSD.
     */
    private static final float PTSD_STRESS_GAIN = 1.3f;

    /**
     * Multiplier applied to positive stress gains when the TARDIS is devoted to its owner.
     */
    private static final float DEVOTED_STRESS_GAIN = 0.75f;

    /**
     * Trust lost when a TARDIS is betrayed by a player.
     */
    private static final float BETRAYAL_TRUST_LOSS = 40f;
    private static final float FORGIVE_BETRAYAL_TRUST = 40f;

    /**
     * Fear required for the TARDIS to be considered afraid of a player.
     */
    private static final float AFRAID_OF = FearLevel.AFRAID_AT;

    /**
     * Describes the result of a TARDIS refusing to take off.
     *
     * <p>The result can indicate that the TARDIS is willing, frightened into
     * cooperating, or refusing for another reason.</p>
     */
    public enum Refusal {
        /**
         * The TARDIS has no reason to refuse.
         */
        NONE,

        /**
         * The TARDIS is frightened enough to cooperate despite its unwillingness.
         */
        COWED,

        /**
         * The TARDIS is resisting but has not reached a full refusal.
         */
        BALK,

        /**
         * The TARDIS has become worked up and is actively resisting.
         */
        WORKED_UP,

        /**
         * The TARDIS is refusing to cooperate.
         */
        REFUSE
    }

    /**
     * Prevents this utility class from being instantiated.
     */
    private TardisMindSystem() {
    }

    /**
     * Initialises the TARDIS mind systems and registers the relevant event hooks.
     *
     * <p>Also registers personality and trait actions for take-off and landing.</p>
     */
    public static void init() {
        TardisMoods.init();
        TardisTurbulence.init();
        TardisDyslexia.init();
        TardisDiscovery.init();
        TardisTrauma.init();

        TardisEvents.TAKE_OFF.register(tardis ->
                runHook(tardis, TardisPersonality::onTakeOff, TardisTrait::onTakeOff));

        TardisEvents.LAND.register(tardis ->
                runHook(tardis, TardisPersonality::onLand, TardisTrait::onLand));
    }

    /**
     * Clears temporary state held by the different mind systems.
     *
     * <p>Does not reset the TARDIS's saved personality, traits or relationships.</p>
     */
    public static void resetState() {
        MindMessages.reset();
        LAST_GENTLE_USE.clear();
        TardisConvincing.reset();
        TardisAnger.reset();
        TardisMallet.reset();
        TardisTrauma.reset();
        TardisTerror.reset();
        TardisFeedback.reset();
        TardisTurbulence.reset();
        TardisPeaceOffering.reset();
        TardisFright.reset();
    }

    /**
     * Gets the current game tick from the server's overworld.
     *
     * <p>The overworld is used as the shared clock for tracking time-sensitive
     * mind values.</p>
     *
     * @param server the Minecraft server
     * @return the current overworld game tick
     */
    public static long now(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    /**
     * Gets the saved mind data belonging to a TARDIS.
     *
     * @param tardis the TARDIS to retrieve the mind from
     * @return its current mind data
     */
    public static TardisMind mind(Tardis tardis) {
        return tardis.data().mind(tardis.id());
    }

    /**
     * Saves updated mind data for a TARDIS.
     *
     * @param tardis the TARDIS whose mind should be updated
     * @param mind   the new mind data
     */
    private static void save(Tardis tardis, TardisMind mind) {
        tardis.data().setMind(tardis.id(), mind);
    }

    /**
     * Gets the TARDIS's current stress after accounting for time-based changes.
     *
     * @param tardis the TARDIS to check
     * @return its current stress level
     */
    public static float stress(Tardis tardis) {
        return mind(tardis).stressAt(now(tardis.server()));
    }

    /**
     * Resolves which player should be used for relationship checks.
     *
     * <p>A {@code null} player means the TARDIS's owner. This is useful for
     * actions that do not have a specific player attached, such as remote
     * controls or commands.</p>
     *
     * @param tardis the TARDIS whose owner should be used as the fallback
     * @param player the player to resolve, or {@code null} for the owner
     * @return the supplied player UUID, or the owner's UUID
     */
    private static UUID who(Tardis tardis, @Nullable UUID player) {
        return player != null ? player : tardis.ownerId();
    }

    /**
     * Gets the TARDIS's relationship with a player.
     *
     * <p>If no player is supplied, the owner's relationship is returned.</p>
     *
     * @param tardis the TARDIS to check
     * @param player the player's UUID, or {@code null} for the owner
     * @return the player's bond with the TARDIS
     */
    public static TardisBond bond(Tardis tardis, @Nullable UUID player) {
        return mind(tardis).bond(who(tardis, player), tardis.ownerId());
    }

    /**
     * Saves an updated relationship with a player.
     *
     * @param tardis the TARDIS whose relationship should be updated
     * @param player the player's UUID, or {@code null} for the owner
     * @param bond   the updated relationship data
     */
    private static void saveBond(Tardis tardis, @Nullable UUID player, TardisBond bond) {
        save(tardis, mind(tardis).withBond(who(tardis, player), bond));
    }

    /**
     * Gets a player's current fear level after accounting for time-based changes.
     *
     * @param tardis the TARDIS to check
     * @param player the player's UUID, or {@code null} for the owner
     * @return the player's current fear level
     */
    public static float fear(Tardis tardis, @Nullable UUID player) {
        TardisMind mind = mind(tardis);
        return mind.fearAt(mind.bond(who(tardis, player), tardis.ownerId()), now(tardis.server()));
    }

    /**
     * Sets how afraid the TARDIS is of a player.
     *
     * <p>Also records the current game tick so that time-based fear changes
     * can be calculated correctly.</p>
     *
     * @param tardis the TARDIS to update
     * @param player the player's UUID, or {@code null} for the owner
     * @param value  the new fear level
     */
    public static void setFear(Tardis tardis, @Nullable UUID player, float value) {
        saveBond(tardis, player, bond(tardis, player).withFear(value, now(tardis.server())));
    }

    /**
     * Changes how afraid the TARDIS is of a player.
     *
     * @param tardis the TARDIS to update
     * @param player the player's UUID, or {@code null} for the owner
     * @param amount the amount of fear to add (negative values reduce fear)
     */
    public static void addFear(Tardis tardis, @Nullable UUID player, float amount) {
        float current = fear(tardis, player);
        saveBond(tardis, player, bond(tardis, player).withFear(current + amount, now(tardis.server())));
    }

    /**
     * Checks whether the TARDIS is frightened enough of a player to obey them.
     *
     * <p>A TARDIS affected by an active terror effect is always considered
     * cowed. Otherwise, its personality must allow fear-based compliance and
     * the player's fear level must reach its compliance threshold.</p>
     *
     * @param tardis the TARDIS to check
     * @param player the player's UUID, or {@code null} for the owner
     * @return whether the TARDIS is frightened into cooperating
     */
    public static boolean cowed(Tardis tardis, @Nullable UUID player) {
        if (TardisTerror.compliant(tardis, player)) return true;

        TardisPersonality personality = mind(tardis).personality();
        return personality.refusesToFly() && fear(tardis, player) >= personality.fearToComply();
    }

    /**
     * Checks whether the TARDIS is afraid of a player.
     *
     * @param tardis the TARDIS to check
     * @param player the player's UUID, or {@code null} for the owner
     * @return whether the player's fear level has reached {@link #AFRAID_OF}
     */
    public static boolean afraidOf(Tardis tardis, @Nullable UUID player) {
        return fear(tardis, player) >= AFRAID_OF;
    }

    /**
     * Gets how much the TARDIS trusts a player.
     *
     * @param tardis the TARDIS to check
     * @param player the player's UUID, or {@code null} for the owner
     * @return the player's current trust level
     */
    public static float trust(Tardis tardis, @Nullable UUID player) {
        return bond(tardis, player).trust();
    }

    /**
     * Sets how much the TARDIS trusts a player.
     *
     * @param tardis the TARDIS to update
     * @param player the player's UUID, or {@code null} for the owner
     * @param value  the new trust level
     */
    public static void setTrust(Tardis tardis, @Nullable UUID player, float value) {
        saveBond(tardis, player, bond(tardis, player).withTrust(value));
    }

    /**
     * Sets the TARDIS's stress level.
     *
     * <p>Records the current game tick so that future stress calculations
     * use the correct starting point.</p>
     *
     * @param tardis the TARDIS to update
     * @param value  the new stress level
     */
    public static void setStress(Tardis tardis, float value) {
        save(tardis, mind(tardis).withStress(value, now(tardis.server())));
    }

    /**
     * Changes the TARDIS's personality.
     *
     * <p>Traits that are incompatible with the new personality are removed.</p>
     *
     * @param tardis      the TARDIS to update
     * @param personality the new personality
     */
    public static void setPersonality(Tardis tardis, TardisPersonality personality) {
        save(tardis, mind(tardis).withPersonality(personality));
    }

    /**
     * Replaces the TARDIS's current traits.
     *
     * @param tardis the TARDIS to update
     * @param traits the new list of traits
     */
    public static void setTraits(Tardis tardis, List<TardisTrait> traits) {
        save(tardis, mind(tardis).withTraits(traits));
    }

    /**
     * Replaces the TARDIS's current disabilities.
     *
     * @param tardis       the TARDIS to update
     * @param disabilities the new list of disabilities
     */
    public static void setDisabilities(Tardis tardis, List<TardisDisability> disabilities) {
        save(tardis, mind(tardis).withDisabilities(disabilities));
    }

    /**
     * Replaces the list of traits and behaviours the TARDIS has discovered
     * about itself.
     *
     * @param tardis     the TARDIS to update
     * @param discovered the new list of discovered entries
     */
    public static void setDiscovered(Tardis tardis, List<String> discovered) {
        save(tardis, mind(tardis).withDiscovered(discovered));
    }

    /**
     * Gets the TARDIS's current skill level.
     *
     * @param tardis the TARDIS to check
     * @return its skill level
     */
    public static float skill(Tardis tardis) {
        return mind(tardis).skill();
    }

    /**
     * Sets the TARDIS's skill level.
     *
     * @param tardis the TARDIS to update
     * @param value  the new skill level
     */
    public static void setSkill(Tardis tardis, float value) {
        save(tardis, mind(tardis).withSkill(value));
    }

    /**
     * Records that the TARDIS has landed at the current game tick.
     *
     * @param tardis the TARDIS that has landed
     */
    public static void markLanded(Tardis tardis) {
        save(tardis, mind(tardis).landed(now(tardis.server())));
    }

    /**
     * Generates a completely new mind for the TARDIS.
     *
     * <p>Also clears any temporary anger or convincing state associated
     * with its previous mind.</p>
     *
     * @param tardis the TARDIS whose mind should be regenerated
     */
    public static void reroll(Tardis tardis) {
        save(tardis, TardisMind.roll(UUID.randomUUID()));
        forget(tardis);
    }

    /**
     * Clears temporary refusal-related state for a TARDIS.
     *
     * <p>This prevents previous convincing attempts or anger from carrying
     * over into a new interaction.</p>
     *
     * @param tardis the TARDIS to clear
     */
    private static void forget(Tardis tardis) {
        TardisConvincing.forget(tardis);
        TardisAnger.forget(tardis);
    }

    /**
     * Changes the TARDIS's stress level.
     *
     * <p>Positive stress gains are affected by trait modifiers and disabilities.
     * PTSD increases stress gained, while high trust in the owner reduces it.</p>
     *
     * <p>Negative amounts are not affected by these positive stress gain
     * modifiers.</p>
     *
     * @param tardis the TARDIS to update
     * @param amount the amount of stress to add (negative values reduce stress)
     */
    public static void addStress(Tardis tardis, float amount) {
        TardisMind mind = mind(tardis);

        if (amount > 0f) {
            amount *= mind.modifiers().stressGain();
        }

        if (amount > 0f) {
            /* ptsd makes it jumpier, less so as it recovers */
            amount *= 1f + (PTSD_STRESS_GAIN - 1f) * mind.ptsdStrength();
        }

        if (amount > 0f && trust(tardis, null) >= DEVOTED_TRUST) {
            amount *= DEVOTED_STRESS_GAIN;
        }

        long now = now(tardis.server());
        save(tardis, mind.withStress(mind.stressAt(now) + amount, now));

        // Only reveal this behaviour when stress actually increases.
        if (amount > 0f) TardisDiscovery.reveal(tardis, Reveal.IT_GETS_STRESSED);
    }

    /**
     * Changes how much the TARDIS trusts a player.
     *
     * <p>Trust gains and losses use separate trait modifiers. The change is
     * also reported to the feedback system so the TARDIS can react to it.</p>
     *
     * @param tardis the TARDIS to update
     * @param player the player's UUID, or {@code null} for the owner
     * @param amount the amount of trust to add (negative values reduce trust)
     */
    public static void addTrust(Tardis tardis, @Nullable UUID player, float amount) {
        TardisBond bond = bond(tardis, player);
        TraitModifiers modifiers = mind(tardis).modifiersFor(bond);

        amount *= amount > 0f ? modifiers.trustGain() : modifiers.trustLoss();

        TardisBond updated = bond.withTrust(bond.trust() + amount);
        /* earning enough trust back makes it forgive a betrayal */
        boolean forgiven = updated.betrayed() && updated.trust() >= FORGIVE_BETRAYAL_TRUST;
        if (forgiven) updated = updated.withBetrayed(false);
        saveBond(tardis, player, updated);

        TardisFeedback.trustChanged(
                tardis,
                updated.trust() - bond.trust(),
                TardisFeedback.Cause.NORMAL
        );
        if (forgiven) {
            ServerPlayer forgivenPlayer = tardis.server().getPlayerList().getPlayer(who(tardis, player));
            if (forgivenPlayer != null) MindMessages.say(forgivenPlayer, ChatFormatting.LIGHT_PURPLE, "tardis_forgives_you");
        }

        if (amount < 0f) TardisDiscovery.reveal(tardis, Reveal.IT_LOSES_TRUST);
    }

    /**
     * Records that a player deliberately hurt the TARDIS.
     *
     * <p>Increases the number of times the player has hurt it and reduces
     * their trust. The amount of trust lost is affected by the TARDIS's
     * abuse modifier.</p>
     *
     * @param tardis    the TARDIS that was hurt
     * @param player    the player responsible
     * @param trustLoss the base amount of trust to remove
     */
    public static void abuse(Tardis tardis, UUID player, float trustLoss) {
        TardisBond bond = bond(tardis, player);
        float loss = trustLoss * mind(tardis).modifiers().abuseTrustLoss();

        TardisBond updated = bond.withHits(bond.hits() + 1).withTrust(bond.trust() - loss);
        saveBond(tardis, player, updated);

        TardisFeedback.trustChanged(
                tardis,
                updated.trust() - bond.trust(),
                TardisFeedback.Cause.ABUSE
        );
    }

    /**
     * Records a betrayal by a player.
     *
     * <p>Betrayal causes a large loss of trust and marks the player as having
     * betrayed the TARDIS. A player can only trigger this once unless the
     * betrayal state is manually reset.</p>
     *
     * @param tardis the TARDIS that was betrayed
     * @param player the player responsible
     * @return {@code true} if the betrayal was recorded, or {@code false}
     * if the TARDIS cannot be betrayed or the player has already betrayed it
     */
    public static boolean betray(Tardis tardis, UUID player) {
        TardisBond bond = bond(tardis, player);

        if (!mind(tardis).modifiers().betrayable() || bond.betrayed()) return false;

        TardisBond updated = bond.withBetrayed(true).withTrust(bond.trust() - BETRAYAL_TRUST_LOSS);
        saveBond(tardis, player, updated);

        TardisFeedback.trustChanged(
                tardis,
                updated.trust() - bond.trust(),
                TardisFeedback.Cause.BETRAYAL
        );

        return true;
    }

    /**
     * Gets the TARDIS's current trauma level.
     *
     * @param tardis the TARDIS to check
     * @return its trauma level
     */
    public static float trauma(Tardis tardis) {
        return mind(tardis).traumaAt(now(tardis.server()));
    }

    /**
     * Sets the TARDIS's trauma level.
     *
     * @param tardis the TARDIS to update
     * @param value  the new trauma level
     */
    public static void setTrauma(Tardis tardis, float value) {
        save(tardis, mind(tardis).withTrauma(value, now(tardis.server())));
    }

    /* scars from one player */
    public static float traumaFrom(Tardis tardis, @Nullable UUID player) {
        return bond(tardis, player).traumaAt(now(tardis.server()));
    }

    public static void setTraumaFrom(Tardis tardis, @Nullable UUID player, float value) {
        saveBond(tardis, player, bond(tardis, player).withTrauma(value, now(tardis.server())));
    }

    public static void setHits(Tardis tardis, @Nullable UUID player, int value) {
        saveBond(tardis, player, bond(tardis, player).withHits(Math.max(0, value)));
    }

    /* wipes everything bad between it and this player. fear, trauma, hits, betrayal */
    public static void forgive(Tardis tardis, @Nullable UUID player) {
        long now = now(tardis.server());
        TardisBond bond = bond(tardis, player);
        saveBond(tardis, player, bond.withFear(0f, now).withTrauma(0f, now).withHits(0).withBetrayed(false));
    }

    /* kindness from this player forgives one hit and eases their scars */
    public static void forgiveOneHit(Tardis tardis, @Nullable UUID player, float traumaEased) {
        TardisBond bond = bond(tardis, player);
        long now = now(tardis.server());
        float trauma = Math.max(0f, bond.traumaAt(now) - traumaEased);
        saveBond(tardis, player, bond.withHits(bond.hits() - 1).withTrauma(trauma, now));
    }

    public static float recovery(Tardis tardis) {
        return mind(tardis).recovery();
    }

    public static void setRecovery(Tardis tardis, float value) {
        save(tardis, mind(tardis).withRecovery(value));
    }

    /* how scared it is of this player, as one simple step */
    public static FearLevel fearLevel(Tardis tardis, @Nullable UUID player) {
        return FearLevel.of(fear(tardis, player));
    }

    /**
     * Sets whether a player has betrayed the TARDIS.
     *
     * <p>This can be used to manually mark or clear a betrayal without
     * changing the player's trust level.</p>
     *
     * @param tardis   the TARDIS to update
     * @param player   the player's UUID, or {@code null} for the owner
     * @param betrayed whether the player should be marked as having betrayed it
     */
    public static void setBetrayed(Tardis tardis, @Nullable UUID player, boolean betrayed) {
        saveBond(tardis, player, bond(tardis, player).withBetrayed(betrayed));
    }

    /**
     * Checks whether a player has ever hurt the TARDIS.
     *
     * @param tardis the TARDIS to check
     * @param player the player to check
     * @return whether the player's recorded hit count is greater than zero
     */
    public static boolean hasHurt(Tardis tardis, UUID player) {
        return bond(tardis, player).hurtIt();
    }

    /**
     * Checks whether the TARDIS is unwilling to cooperate with its owner.
     *
     * @param tardis the TARDIS to check
     * @return whether it is too stressed or distrustful to cooperate
     */
    public static boolean unwilling(Tardis tardis) {
        return unwilling(tardis, null);
    }

    /**
     * Checks whether the TARDIS is unwilling to cooperate with a pilot.
     *
     * <p>A TARDIS is unwilling when its stress is too high or its trust
     * in the pilot is too low.</p>
     *
     * @param tardis the TARDIS to check
     * @param pilot  the pilot's UUID, or {@code null} for the owner
     * @return whether the TARDIS is unwilling to cooperate
     */
    public static boolean unwilling(Tardis tardis, @Nullable UUID pilot) {
        return tooStressed(tardis, pilot) || distrusts(tardis, pilot);
    }

    /**
     * Checks whether the TARDIS has reached its stress limit.
     *
     * <p>The limit is adjusted by personality and trait modifiers. A positive
     * limit shift lowers the amount of stress needed for refusal.</p>
     *
     * @param tardis the TARDIS to check
     * @param pilot  the pilot's UUID, or {@code null} for the owner
     * @return whether its current stress has reached or exceeded its limit
     */
    public static boolean tooStressed(Tardis tardis, @Nullable UUID pilot) {
        TardisMind mind = mind(tardis);
        float limit = mind.personality().stressLimit() - limitShift(tardis, mind, pilot);

        return mind.stressAt(now(tardis.server())) >= limit;
    }

    /**
     * Checks whether the TARDIS trusts a pilot enough to cooperate.
     *
     * <p>The required trust level is adjusted by personality and trait modifiers.
     * A positive limit shift makes the TARDIS require more trust.</p>
     *
     * @param tardis the TARDIS to check
     * @param pilot  the pilot's UUID, or {@code null} for the owner
     * @return whether the pilot's trust level is below the required limit
     */
    public static boolean distrusts(Tardis tardis, @Nullable UUID pilot) {
        TardisMind mind = mind(tardis);
        float limit = mind.personality().trustLimit() + limitShift(tardis, mind, pilot);

        return trust(tardis, pilot) < limit;
    }

    /**
     * Calculates how much the TARDIS's refusal limits should change.
     *
     * <p>Applies the general refusal modifier and, when the pilot is not the
     * owner, the additional stranger modifier.</p>
     *
     * <p>A positive result makes the TARDIS more likely to refuse:
     * it lowers the stress limit and raises the required trust level.</p>
     *
     * @param tardis the TARDIS being checked
     * @param mind   its current mind data
     * @param pilot  the pilot's UUID, or {@code null} for the owner
     * @return the combined change to the refusal limits
     */
    private static float limitShift(Tardis tardis, TardisMind mind, @Nullable UUID pilot) {
        TraitModifiers modifiers = mind.modifiers();
        float shift = modifiers.refusalShift();

        // Stranger modifiers only apply when the pilot is not the owner.
        if (pilot != null && !pilot.equals(tardis.ownerId())) {
            shift += modifiers.strangerShift();
        }

        return shift;
    }

    /**
     * Reveals that strangers upset the TARDIS when the conditions are met.
     *
     * <p>This only applies to non-owner pilots. The discovery is revealed
     * when the TARDIS is sufficiently stressed or unwilling to cooperate.</p>
     *
     * @param tardis the TARDIS being checked
     * @param pilot  the current pilot, or {@code null} for the owner
     */
    private static void revealStrangerTraits(Tardis tardis, @Nullable UUID pilot) {
        if (pilot == null || pilot.equals(tardis.ownerId())) return;

        // Avoid revealing this behaviour unless stress or unwillingness is high enough.
        if (stress(tardis) < STRANGER_REVEAL_STRESS && !unwilling(tardis, pilot)) return;

        TardisDiscovery.reveal(tardis, Reveal.A_STRANGER_UPSETS_IT);
    }

    /**
     * Checks whether the TARDIS refuses to take off with the current pilot.
     *
     * <p>If the TARDIS is willing, any temporary refusal state is cleared.
     * Otherwise, its refusal behaviour is processed using its traits and
     * personality.</p>
     *
     * <p>A frightened TARDIS may still cooperate, but doing so increases
     * its stress and reduces its trust in the pilot.</p>
     *
     * @param tardis the TARDIS being asked to take off
     * @param pilot  the pilot's UUID, or {@code null} for the owner
     * @return the result of the TARDIS's refusal check
     */
    public static Refusal refusesTakeoff(Tardis tardis, @Nullable UUID pilot) {
        revealStrangerTraits(tardis, pilot);

        if (!unwilling(tardis, pilot)) {
            forget(tardis);
            rewardGentleUse(tardis, pilot);
            return Refusal.NONE;
        }

        TardisDiscovery.personality(tardis);
        TardisDiscovery.reveal(tardis, Reveal.IT_REFUSES);

        if (cowed(tardis, pilot)) {
            forget(tardis);
            addStress(tardis, COWED_STRESS);
            addTrust(tardis, pilot, -COWED_TRUST_LOSS);
            setTraumaFrom(tardis, pilot, traumaFrom(tardis, pilot) + COWED_TRAUMA);
            return Refusal.COWED;
        }

        TardisMind mind = mind(tardis);
        MindContext context = MindContext.of(tardis);
        context.pilot = pilot;

        // Traits get the first chance to decide how the TARDIS responds.
        for (TardisTrait trait : mind.traits()) {
            MindActions.run(trait.onUnwilling(), context);
        }

        // The personality is only checked if no trait has already chosen a response.
        if (context.refusal == null) {
            MindActions.run(mind.personality().onUnwilling(), context);
        }

        return context.refusal == null ? Refusal.NONE : context.refusal;
    }

    /* flying it when it's happy to go builds trust a little. once a minute per player so the lever can't be farmed */
    private static void rewardGentleUse(Tardis tardis, @Nullable UUID pilot) {
        UUID player = who(tardis, pilot);
        long now = now(tardis.server());
        String key = tardis.id() + "/" + player;
        Long last = LAST_GENTLE_USE.get(key);
        if (last != null && now - last < GENTLE_USE_COOLDOWN_TICKS) return;
        LAST_GENTLE_USE.put(key, now);
        addTrust(tardis, player, GENTLE_USE_TRUST);
    }

    /**
     * Runs the appropriate actions when a TARDIS event occurs.
     *
     * <p>The personality's actions run first, followed by the actions
     * belonging to each of the TARDIS's traits.</p>
     *
     * @param tardis          the TARDIS that triggered the event
     * @param personalityHook gets the actions associated with the event
     *                        from its personality
     * @param traitHook       gets the actions associated with the event
     *                        from each of its traits
     */
    private static void runHook(
            Tardis tardis,
            Function<TardisPersonality, List<MindAction>> personalityHook,
            Function<TardisTrait, List<MindAction>> traitHook
    ) {
        TardisMind mind = mind(tardis);
        MindContext context = MindContext.of(tardis);

        MindActions.run(personalityHook.apply(mind.personality()), context);

        for (TardisTrait trait : mind.traits()) {
            MindActions.run(traitHook.apply(trait), context);
        }
    }
}