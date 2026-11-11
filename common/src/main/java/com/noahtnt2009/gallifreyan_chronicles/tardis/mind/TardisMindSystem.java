package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisMallet;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisTrauma;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDyslexia;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;
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
    private static final float PTSD_STRESS_GAIN = 1.3f;
    private static final float DEVOTED_STRESS_GAIN = 0.75f;
    private static final float BETRAYAL_TRUST_LOSS = 40f;

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

    public static float fear(Tardis tardis) {
        return mind(tardis).fearAt(now(tardis.server()));
    }

    public static void setFear(Tardis tardis, float value) {
        save(tardis, mind(tardis).withFear(value, now(tardis.server())));
    }

    public static void addFear(Tardis tardis, float amount) {
        TardisMind mind = mind(tardis);
        long now = now(tardis.server());
        save(tardis, mind.withFear(mind.fearAt(now) + amount, now));
    }

    public static boolean cowed(Tardis tardis) {
        if (TardisTerror.compliant(tardis)) return true;
        TardisPersonality personality = mind(tardis).personality();
        return !personality.alwaysFlies() && fear(tardis) >= personality.fearToComply();
    }

    public static float trust(Tardis tardis) {
        return mind(tardis).trust();
    }

    public static void setStress(Tardis tardis, float value) {
        save(tardis, mind(tardis).withStress(value, now(tardis.server())));
    }

    public static void setTrust(Tardis tardis, float value) {
        save(tardis, mind(tardis).withTrust(value));
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
        if (amount > 0f && mind.trust() >= DEVOTED_TRUST) {
            amount *= DEVOTED_STRESS_GAIN;
        }
        long now = now(tardis.server());
        save(tardis, mind.withStress(mind.stressAt(now) + amount, now));
        if (amount > 0f) TardisDiscovery.reveal(tardis, Reveal.IT_GETS_STRESSED);
    }

    public static void addTrust(Tardis tardis, float amount) {
        TardisMind mind = mind(tardis);
        TraitModifiers modifiers = mind.modifiers();
        amount *= amount > 0f ? modifiers.trustGain() : modifiers.trustLoss();
        save(tardis, mind.withTrust(mind.trust() + amount));
        TardisDiscovery.reveal(tardis, Reveal.ITS_TRUST_CHANGES);
    }

    /* hurting it on purpose. distrusting ones never forget it */
    public static void abuse(Tardis tardis, float trustLoss) {
        TardisMind mind = mind(tardis);
        float loss = trustLoss * mind.modifiers().abuseTrustLoss();
        save(tardis, mind.withTrust(mind.trust() - loss));
    }

    /* a loyal tardis turned on. trust crashes and is very hard to win back */
    public static boolean betray(Tardis tardis) {
        TardisMind mind = mind(tardis);
        if (!mind.modifiers().betrayable() || mind.betrayed()) return false;
        save(tardis, mind.withBetrayed(true).withTrust(mind.trust() - BETRAYAL_TRUST_LOSS));
        return true;
    }

    public static float trauma(Tardis tardis) {
        return mind(tardis).trauma();
    }

    public static void setTrauma(Tardis tardis, float value) {
        save(tardis, mind(tardis).withTrauma(value));
    }

    public static void setBetrayed(Tardis tardis, boolean betrayed) {
        save(tardis, mind(tardis).withBetrayed(betrayed));
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
        return mind.trust() < limit;
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
        TardisDiscovery.reveal(tardis, Reveal.A_STRANGER_FLIES_IT);
    }

    public static Refusal refusesTakeoff(Tardis tardis, @Nullable UUID pilot) {
        revealStrangerTraits(tardis, pilot);
        if (!unwilling(tardis, pilot)) {
            forget(tardis);
            return Refusal.NONE;
        }
        TardisDiscovery.personality(tardis);
        TardisDiscovery.reveal(tardis, Reveal.IT_REFUSES);
        if (cowed(tardis)) {
            forget(tardis);
            addStress(tardis, COWED_STRESS);
            addTrust(tardis, -COWED_TRUST_LOSS);
            return Refusal.COWED;
        }

        TardisMind mind = mind(tardis);
        MindContext context = MindContext.of(tardis);
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
