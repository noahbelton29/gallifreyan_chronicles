package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.FearLevel;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisBond;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisMallet;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisTrauma;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindActions;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDyslexia;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.feedback.TardisFeedback;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.flight.TardisTurbulence;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.fright.TardisFright;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisMoods;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.offering.TardisPeaceOffering;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal.TardisAnger;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal.TardisConvincing;

/**
 * Handles the mood of every TARDIS, stress, fear and trust.
 * It uses the personality and traits to decide how the TARDIS reacts to its pilot.
 */
public final class TardisMindSystem {

    private static final float COWED_STRESS = 4f;
    private static final float GENTLE_USE_TRUST = 1f;
    private static final long GENTLE_USE_COOLDOWN_TICKS = 1200L;
    private static final Map<String, Long> LAST_GENTLE_USE = new HashMap<>();

    // Forcing it with fear works but it costs, kindness should always be the better way
    private static final float COWED_TRUST_LOSS = 4f;
    private static final float COWED_TRAUMA = 1f;

    private static final float DEVOTED_TRUST = 75f;

    private static final float STRANGER_REVEAL_STRESS = 25f;

    private static final float PTSD_STRESS_GAIN = 1.3f;

    private static final float DEVOTED_STRESS_GAIN = 0.75f;

    private static final float BETRAYAL_TRUST_LOSS = 40f;
    private static final float FORGIVE_BETRAYAL_TRUST = 40f;

    private static final float AFRAID_OF = FearLevel.AFRAID_AT;

    /** How a TARDIS answers when asked to take off. */
    public enum Refusal {
        NONE,

        // Too scared to say no, so it goes along with it
        COWED,

        // Resisting a little but not refusing yet
        BALK,

        // Upset and fighting back
        WORKED_UP,

        // Will not go
        REFUSE
    }

    private TardisMindSystem() {}

    public static void init() {
        TardisMoods.init();
        TardisTurbulence.init();
        TardisDyslexia.init();
        TardisDiscovery.init();
        TardisTrauma.init();

        TardisEvents.TAKE_OFF.register(tardis -> runHook(tardis, TardisPersonality::onTakeOff, TardisTrait::onTakeOff));

        TardisEvents.LAND.register(tardis -> runHook(tardis, TardisPersonality::onLand, TardisTrait::onLand));
    }

    /** Only clears temporary state, the saved personality and traits stay. */
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

    /** The overworld tick is the clock for every mind timer. */
    public static long now(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    public static TardisMind mind(Tardis tardis) {
        return tardis.data().mind(tardis.id());
    }

    private static void save(Tardis tardis, TardisMind mind) {
        tardis.data().setMind(tardis.id(), mind);
    }

    /** Includes the change over time since it was last updated. */
    public static float stress(Tardis tardis) {
        return mind(tardis).stressAt(now(tardis.server()));
    }

    /** No player means the owner. */
    private static UUID who(Tardis tardis, @Nullable UUID player) {
        return player != null ? player : tardis.ownerId();
    }

    public static TardisBond bond(Tardis tardis, @Nullable UUID player) {
        return mind(tardis).bond(who(tardis, player), tardis.ownerId());
    }

    private static void saveBond(Tardis tardis, @Nullable UUID player, TardisBond bond) {
        save(tardis, mind(tardis).withBond(who(tardis, player), bond));
    }

    /** Includes the change over time since it was last updated. */
    public static float fear(Tardis tardis, @Nullable UUID player) {
        TardisMind mind = mind(tardis);
        return mind.fearAt(mind.bond(who(tardis, player), tardis.ownerId()), now(tardis.server()));
    }

    /** Also saves the tick so the change over time starts from now. */
    public static void setFear(Tardis tardis, @Nullable UUID player, float value) {
        saveBond(tardis, player, bond(tardis, player).withFear(value, now(tardis.server())));
    }

    public static void addFear(Tardis tardis, @Nullable UUID player, float amount) {
        float current = fear(tardis, player);
        saveBond(tardis, player, bond(tardis, player).withFear(current + amount, now(tardis.server())));
    }

    /** A TARDIS under terror is always cowed, otherwise the personality has to allow it and the fear has to be high enough. */
    public static boolean cowed(Tardis tardis, @Nullable UUID player) {
        if (TardisTerror.compliant(tardis, player)) return true;

        TardisPersonality personality = mind(tardis).personality();
        return personality.refusesToFly() && fear(tardis, player) >= personality.fearToComply();
    }

    public static boolean afraidOf(Tardis tardis, @Nullable UUID player) {
        return fear(tardis, player) >= AFRAID_OF;
    }

    public static float trust(Tardis tardis, @Nullable UUID player) {
        return bond(tardis, player).trust();
    }

    public static void setTrust(Tardis tardis, @Nullable UUID player, float value) {
        saveBond(tardis, player, bond(tardis, player).withTrust(value));
    }

    /** Also saves the tick so the change over time starts from now. */
    public static void setStress(Tardis tardis, float value) {
        save(tardis, mind(tardis).withStress(value, now(tardis.server())));
    }

    /** Traits that do not suit the new personality are dropped. */
    public static void setPersonality(Tardis tardis, TardisPersonality personality) {
        save(tardis, mind(tardis).withPersonality(personality));
    }

    public static void setTraits(Tardis tardis, List<TardisTrait> traits) {
        save(tardis, mind(tardis).withTraits(traits));
    }

    public static void setDisabilities(Tardis tardis, List<TardisDisability> disabilities) {
        save(tardis, mind(tardis).withDisabilities(disabilities));
    }

    public static void setDiscovered(Tardis tardis, List<String> discovered) {
        save(tardis, mind(tardis).withDiscovered(discovered));
    }

    public static float skill(Tardis tardis) {
        return mind(tardis).skill();
    }

    public static void setSkill(Tardis tardis, float value) {
        save(tardis, mind(tardis).withSkill(value));
    }

    public static void markLanded(Tardis tardis) {
        save(tardis, mind(tardis).landed(now(tardis.server())));
    }

    /** Also clears the anger and convincing state from the old mind. */
    public static void reroll(Tardis tardis) {
        save(tardis, TardisMind.roll(UUID.randomUUID()));
        forget(tardis);
    }

    /** So earlier convincing and anger do not carry over. */
    private static void forget(Tardis tardis) {
        TardisConvincing.forget(tardis);
        TardisAnger.forget(tardis);
    }

    /**
     * Only gains are changed by traits and disabilities, PTSD makes them bigger and high trust in the owner makes them smaller.
     * Losses are left alone.
     */
    public static void addStress(Tardis tardis, float amount) {
        TardisMind mind = mind(tardis);

        if (amount > 0f) {
            amount *= mind.modifiers().stressGain();
        }

        if (amount > 0f) {
            // PTSD makes it jumpier, less so as it recovers
            amount *= 1f + (PTSD_STRESS_GAIN - 1f) * mind.ptsdStrength();
        }

        if (amount > 0f && trust(tardis, null) >= DEVOTED_TRUST) {
            amount *= DEVOTED_STRESS_GAIN;
        }

        long now = now(tardis.server());
        save(tardis, mind.withStress(mind.stressAt(now) + amount, now));

        if (amount > 0f) TardisDiscovery.reveal(tardis, Reveal.IT_GETS_STRESSED);
    }

    /** Gains and losses use different trait modifiers, the feedback system is told so the TARDIS can react. */
    public static void addTrust(Tardis tardis, @Nullable UUID player, float amount) {
        TardisBond bond = bond(tardis, player);
        TraitModifiers modifiers = mind(tardis).modifiersFor(bond);

        amount *= amount > 0f ? modifiers.trustGain() : modifiers.trustLoss();

        TardisBond updated = bond.withTrust(bond.trust() + amount);
        // Earning enough trust back means it forgives a betrayal
        boolean forgiven = updated.betrayed() && updated.trust() >= FORGIVE_BETRAYAL_TRUST;
        if (forgiven) updated = updated.withBetrayed(false);
        saveBond(tardis, player, updated);

        TardisFeedback.trustChanged(tardis, updated.trust() - bond.trust(), TardisFeedback.Cause.NORMAL);
        if (forgiven) {
            ServerPlayer forgivenPlayer = tardis.server().getPlayerList().getPlayer(who(tardis, player));
            if (forgivenPlayer != null)
                MindMessages.say(forgivenPlayer, ChatFormatting.LIGHT_PURPLE, "tardis_forgives_you");
        }

        if (amount < 0f) TardisDiscovery.reveal(tardis, Reveal.IT_LOSES_TRUST);
    }

    /** Counts a hit and takes away trust, the abuse modifier changes how much. */
    public static void abuse(Tardis tardis, UUID player, float trustLoss) {
        TardisBond bond = bond(tardis, player);
        float loss = trustLoss * mind(tardis).modifiers().abuseTrustLoss();

        TardisBond updated = bond.withHits(bond.hits() + 1).withTrust(bond.trust() - loss);
        saveBond(tardis, player, updated);

        TardisFeedback.trustChanged(tardis, updated.trust() - bond.trust(), TardisFeedback.Cause.ABUSE);
    }

    /**
     * Only works once per player unless the betrayal is reset.
     * Returns false if it cannot be betrayed or already was.
     */
    public static boolean betray(Tardis tardis, UUID player) {
        TardisBond bond = bond(tardis, player);

        if (!mind(tardis).modifiers().betrayable() || bond.betrayed()) return false;

        TardisBond updated = bond.withBetrayed(true).withTrust(bond.trust() - BETRAYAL_TRUST_LOSS);
        saveBond(tardis, player, updated);

        TardisFeedback.trustChanged(tardis, updated.trust() - bond.trust(), TardisFeedback.Cause.BETRAYAL);

        return true;
    }

    public static float trauma(Tardis tardis) {
        return mind(tardis).traumaAt(now(tardis.server()));
    }

    public static void setTrauma(Tardis tardis, float value) {
        save(tardis, mind(tardis).withTrauma(value, now(tardis.server())));
    }

    /** The scars from one player. */
    public static float traumaFrom(Tardis tardis, @Nullable UUID player) {
        return bond(tardis, player).traumaAt(now(tardis.server()));
    }

    public static void setTraumaFrom(Tardis tardis, @Nullable UUID player, float value) {
        saveBond(tardis, player, bond(tardis, player).withTrauma(value, now(tardis.server())));
    }

    public static void setHits(Tardis tardis, @Nullable UUID player, int value) {
        saveBond(tardis, player, bond(tardis, player).withHits(Math.max(0, value)));
    }

    /** Wipes everything bad between it and this player, fear, trauma, hits and betrayal. */
    public static void forgive(Tardis tardis, @Nullable UUID player) {
        long now = now(tardis.server());
        TardisBond bond = bond(tardis, player);
        saveBond(
                tardis,
                player,
                bond.withFear(0f, now).withTrauma(0f, now).withHits(0).withBetrayed(false));
    }

    /** Kindness forgives one hit and eases the scars from that player. */
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

    /** How scared it is of this player as a simple level. */
    public static FearLevel fearLevel(Tardis tardis, @Nullable UUID player) {
        return FearLevel.of(fear(tardis, player));
    }

    /** Marks or clears a betrayal without touching trust. */
    public static void setBetrayed(Tardis tardis, @Nullable UUID player, boolean betrayed) {
        saveBond(tardis, player, bond(tardis, player).withBetrayed(betrayed));
    }

    public static boolean hasHurt(Tardis tardis, UUID player) {
        return bond(tardis, player).hurtIt();
    }

    public static boolean unwilling(Tardis tardis) {
        return unwilling(tardis, null);
    }

    /** Unwilling means too stressed or not trusting the pilot enough. */
    public static boolean unwilling(Tardis tardis, @Nullable UUID pilot) {
        return tooStressed(tardis, pilot) || distrusts(tardis, pilot);
    }

    /** A higher limit shift means less stress is needed to refuse. */
    public static boolean tooStressed(Tardis tardis, @Nullable UUID pilot) {
        TardisMind mind = mind(tardis);
        float limit = mind.personality().stressLimit() - limitShift(tardis, mind, pilot);

        return mind.stressAt(now(tardis.server())) >= limit;
    }

    /** A higher limit shift means more trust is needed. */
    public static boolean distrusts(Tardis tardis, @Nullable UUID pilot) {
        TardisMind mind = mind(tardis);
        float limit = mind.personality().trustLimit() + limitShift(tardis, mind, pilot);

        return trust(tardis, pilot) < limit;
    }

    /**
     * Positive makes it more likely to refuse, it lowers the stress limit and raises the trust needed.
     * Strangers get an extra modifier on top.
     */
    private static float limitShift(Tardis tardis, TardisMind mind, @Nullable UUID pilot) {
        TraitModifiers modifiers = mind.modifiers();
        float shift = modifiers.refusalShift();

        if (pilot != null && !pilot.equals(tardis.ownerId())) {
            shift += modifiers.strangerShift();
        }

        return shift;
    }

    private static void revealStrangerTraits(Tardis tardis, @Nullable UUID pilot) {
        if (pilot == null || pilot.equals(tardis.ownerId())) return;

        if (stress(tardis) < STRANGER_REVEAL_STRESS && !unwilling(tardis, pilot)) return;

        TardisDiscovery.reveal(tardis, Reveal.A_STRANGER_UPSETS_IT);
    }

    /**
     * If it is willing, the temporary refusal state is cleared.
     * A scared TARDIS goes along with it but gets more stressed and trusts the pilot less.
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

        // Traits get the first say
        for (TardisTrait trait : mind.traits()) {
            MindActions.run(trait.onUnwilling(), context);
        }

        // The personality only gets a say if no trait chose
        if (context.refusal == null) {
            MindActions.run(mind.personality().onUnwilling(), context);
        }

        return context.refusal == null ? Refusal.NONE : context.refusal;
    }

    /** Flying when it is happy to go builds a little trust, once a minute per player so the lever cannot be farmed. */
    private static void rewardGentleUse(Tardis tardis, @Nullable UUID pilot) {
        UUID player = who(tardis, pilot);
        long now = now(tardis.server());
        String key = tardis.id() + "/" + player;
        Long last = LAST_GENTLE_USE.get(key);
        if (last != null && now - last < GENTLE_USE_COOLDOWN_TICKS) return;
        LAST_GENTLE_USE.put(key, now);
        addTrust(tardis, player, GENTLE_USE_TRUST);
    }

    /** The personality actions run first, then the actions of each trait. */
    private static void runHook(
            Tardis tardis,
            Function<TardisPersonality, List<MindAction>> personalityHook,
            Function<TardisTrait, List<MindAction>> traitHook) {
        TardisMind mind = mind(tardis);
        MindContext context = MindContext.of(tardis);

        MindActions.run(personalityHook.apply(mind.personality()), context);

        for (TardisTrait trait : mind.traits()) {
            MindActions.run(traitHook.apply(trait), context);
        }
    }
}
