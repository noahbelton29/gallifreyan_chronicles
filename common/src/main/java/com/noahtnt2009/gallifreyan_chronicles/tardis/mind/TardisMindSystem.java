package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisMallet;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisTrauma;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDyslexia;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.feedback.TardisFeedback;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.fright.TardisFright;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.offering.TardisPeaceOffering;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood.TardisMoods;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal.TardisAnger;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal.TardisConvincing;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

public final class TardisMindSystem {
    private static final float COWED_STRESS = 4f;
    private static final float COWED_TRUST_LOSS = 2f;
    private static final float DEVOTED_TRUST = 75f;
    private static final float STRANGER_REVEAL_STRESS = 25f;
    private static final float PTSD_STRESS_GAIN = 1.3f;
    private static final float DEVOTED_STRESS_GAIN = 0.75f;
    private static final float BETRAYAL_TRUST_LOSS = 40f;
    private static final float AFRAID_OF = 10f;

    public enum Refusal {
        NONE, COWED, BALK, WORKED_UP, REFUSE
    }

    private TardisMindSystem() {
    }

    public static void init() {
        TardisMoods.init();
        TardisDyslexia.init();
        TardisDiscovery.init();
        TardisTrauma.init();
        TardisEvents.TAKE_OFF.register(tardis -> runHook(tardis, TardisPersonality::onTakeOff, TardisTrait::onTakeOff));
        TardisEvents.LAND.register(tardis -> runHook(tardis, TardisPersonality::onLand, TardisTrait::onLand));
    }

    public static void resetState() {
        TardisConvincing.reset();
        TardisAnger.reset();
        TardisMallet.reset();
        TardisTrauma.reset();
        TardisTerror.reset();
        TardisFeedback.reset();
        TardisPeaceOffering.reset();
        TardisFright.reset();
    }

    public static long now(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    public static TardisMind mind(Tardis tardis) {
        return tardis.data().mind(tardis.id());
    }

    private static void save(Tardis tardis, TardisMind mind) {
        tardis.data().setMind(tardis.id(), mind);
    }

    public static float stress(Tardis tardis) {
        return mind(tardis).stressAt(now(tardis.server()));
    }

    /* null means the owner, e.g. the remote or a command */
    private static UUID who(Tardis tardis, @Nullable UUID player) {
        return player != null ? player : tardis.ownerId();
    }

    public static TardisBond bond(Tardis tardis, @Nullable UUID player) {
        return mind(tardis).bond(who(tardis, player), tardis.ownerId());
    }

    private static void saveBond(Tardis tardis, @Nullable UUID player, TardisBond bond) {
        save(tardis, mind(tardis).withBond(who(tardis, player), bond));
    }

    public static float fear(Tardis tardis, @Nullable UUID player) {
        TardisMind mind = mind(tardis);
        return mind.fearAt(mind.bond(who(tardis, player), tardis.ownerId()), now(tardis.server()));
    }

    public static void setFear(Tardis tardis, @Nullable UUID player, float value) {
        saveBond(tardis, player, bond(tardis, player).withFear(value, now(tardis.server())));
    }

    public static void addFear(Tardis tardis, @Nullable UUID player, float amount) {
        float current = fear(tardis, player);
        saveBond(tardis, player, bond(tardis, player).withFear(current + amount, now(tardis.server())));
    }

    /* scared enough of this player to do what they want */
    public static boolean cowed(Tardis tardis, @Nullable UUID player) {
        if (TardisTerror.compliant(tardis)) return true;
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

    public static void setStress(Tardis tardis, float value) {
        save(tardis, mind(tardis).withStress(value, now(tardis.server())));
    }

    /* traits that don't suit the new personality are dropped */
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

    public static void reroll(Tardis tardis) {
        save(tardis, TardisMind.roll(UUID.randomUUID()));
        forget(tardis);
    }

    private static void forget(Tardis tardis) {
        TardisConvincing.forget(tardis);
        TardisAnger.forget(tardis);
    }

    public static void addStress(Tardis tardis, float amount) {
        TardisMind mind = mind(tardis);
        if (amount > 0f) {
            amount *= mind.modifiers().stressGain();
        }
        if (amount > 0f && mind.has(TardisDisability.PTSD)) {
            amount *= PTSD_STRESS_GAIN;
        }
        if (amount > 0f && trust(tardis, null) >= DEVOTED_TRUST) {
            amount *= DEVOTED_STRESS_GAIN;
        }
        long now = now(tardis.server());
        save(tardis, mind.withStress(mind.stressAt(now) + amount, now));
        if (amount > 0f) TardisDiscovery.reveal(tardis, Reveal.IT_GETS_STRESSED);
    }

    public static void addTrust(Tardis tardis, @Nullable UUID player, float amount) {
        TardisBond bond = bond(tardis, player);
        TraitModifiers modifiers = mind(tardis).modifiersFor(bond);
        amount *= amount > 0f ? modifiers.trustGain() : modifiers.trustLoss();
        TardisBond updated = bond.withTrust(bond.trust() + amount);
        saveBond(tardis, player, updated);
        TardisFeedback.trustChanged(tardis, updated.trust() - bond.trust(), TardisFeedback.Cause.NORMAL);
        if (amount < 0f) TardisDiscovery.reveal(tardis, Reveal.IT_LOSES_TRUST);
    }

    /* this player hurt it on purpose. distrusting ones never forget it */
    public static void abuse(Tardis tardis, UUID player, float trustLoss) {
        TardisBond bond = bond(tardis, player);
        float loss = trustLoss * mind(tardis).modifiers().abuseTrustLoss();
        TardisBond updated = bond.hit().withTrust(bond.trust() - loss);
        saveBond(tardis, player, updated);
        TardisFeedback.trustChanged(tardis, updated.trust() - bond.trust(), TardisFeedback.Cause.ABUSE);
    }

    /* a loyal tardis turned on by this player. trust crashes and is very hard to win back */
    public static boolean betray(Tardis tardis, UUID player) {
        TardisBond bond = bond(tardis, player);
        if (!mind(tardis).modifiers().betrayable() || bond.betrayed()) return false;
        TardisBond updated = bond.withBetrayed(true).withTrust(bond.trust() - BETRAYAL_TRUST_LOSS);
        saveBond(tardis, player, updated);
        TardisFeedback.trustChanged(tardis, updated.trust() - bond.trust(), TardisFeedback.Cause.BETRAYAL);
        return true;
    }

    public static float trauma(Tardis tardis) {
        return mind(tardis).trauma();
    }

    public static void setTrauma(Tardis tardis, float value) {
        save(tardis, mind(tardis).withTrauma(value));
    }

    public static void setBetrayed(Tardis tardis, @Nullable UUID player, boolean betrayed) {
        saveBond(tardis, player, bond(tardis, player).withBetrayed(betrayed));
    }

    public static boolean hasHurt(Tardis tardis, UUID player) {
        return bond(tardis, player).hurtIt();
    }

    public static boolean unwilling(Tardis tardis) {
        return unwilling(tardis, null);
    }

    public static boolean unwilling(Tardis tardis, @Nullable UUID pilot) {
        return tooStressed(tardis, pilot) || distrusts(tardis, pilot);
    }

    public static boolean tooStressed(Tardis tardis, @Nullable UUID pilot) {
        TardisMind mind = mind(tardis);
        float limit = mind.personality().stressLimit() - limitShift(tardis, mind, pilot);
        return mind.stressAt(now(tardis.server())) >= limit;
    }

    public static boolean distrusts(Tardis tardis, @Nullable UUID pilot) {
        TardisMind mind = mind(tardis);
        float limit = mind.personality().trustLimit() + limitShift(tardis, mind, pilot);
        return trust(tardis, pilot) < limit;
    }

    /* positive makes it say no sooner */
    private static float limitShift(Tardis tardis, TardisMind mind, @Nullable UUID pilot) {
        TraitModifiers modifiers = mind.modifiers();
        float shift = modifiers.refusalShift();
        if (pilot != null && !pilot.equals(tardis.ownerId())) shift += modifiers.strangerShift();
        return shift;
    }

    private static void revealStrangerTraits(Tardis tardis, @Nullable UUID pilot) {
        if (pilot == null || pilot.equals(tardis.ownerId())) return;
        if (stress(tardis) < STRANGER_REVEAL_STRESS && !unwilling(tardis, pilot)) return;
        TardisDiscovery.reveal(tardis, Reveal.A_STRANGER_UPSETS_IT);
    }

    public static Refusal refusesTakeoff(Tardis tardis, @Nullable UUID pilot) {
        revealStrangerTraits(tardis, pilot);
        if (!unwilling(tardis, pilot)) {
            forget(tardis);
            return Refusal.NONE;
        }
        TardisDiscovery.personality(tardis);
        TardisDiscovery.reveal(tardis, Reveal.IT_REFUSES);
        if (cowed(tardis, pilot)) {
            forget(tardis);
            addStress(tardis, COWED_STRESS);
            addTrust(tardis, pilot, -COWED_TRUST_LOSS);
            return Refusal.COWED;
        }

        TardisMind mind = mind(tardis);
        MindContext context = MindContext.of(tardis);
        context.pilot = pilot;
        for (TardisTrait trait : mind.traits()) {
            MindActions.run(trait.onUnwilling(), context);
        }
        if (context.refusal == null) {
            MindActions.run(mind.personality().onUnwilling(), context);
        }
        return context.refusal == null ? Refusal.NONE : context.refusal;
    }

    private static void runHook(Tardis tardis, Function<TardisPersonality, List<MindAction>> personalityHook,
                                Function<TardisTrait, List<MindAction>> traitHook) {
        TardisMind mind = mind(tardis);
        MindContext context = MindContext.of(tardis);
        MindActions.run(personalityHook.apply(mind.personality()), context);
        for (TardisTrait trait : mind.traits()) {
            MindActions.run(traitHook.apply(trait), context);
        }
    }
}
