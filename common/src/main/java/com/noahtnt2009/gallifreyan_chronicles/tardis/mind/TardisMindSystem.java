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
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public final class TardisMindSystem {
    private static final float PESTER_STRESS = 2f;
    private static final float STUBBORN_LIMIT_SHIFT = 10f;
    private static final float SKITTISH_STRESS_GAIN = 1.5f;
    private static final float LOYAL_TRUST_GAIN = 2f;
    private static final float LOYAL_TRUST_LOSS = 0.5f;
    private static final float COWED_STRESS = 4f;
    private static final float COWED_TRUST_LOSS = 2f;
    private static final float POSSESSIVE_STRANGER_SHIFT = 20f;
    private static final float DEVOTED_TRUST = 75f;
    private static final float PTSD_STRESS_GAIN = 1.3f;
    private static final float DEVOTED_STRESS_GAIN = 0.75f;
    private static final float BETRAYED_TRUST_GAIN = 0.2f;
    private static final float BETRAYED_TRUST_LOSS = 1.5f;
    private static final float BETRAYAL_TRUST_LOSS = 40f;
    private static final float DISTRUSTING_TRUST_GAIN = 0.35f;
    private static final float DISTRUSTING_TRUST_LOSS = 0.5f;
    private static final float DISTRUSTING_ABUSE = 4f;
    private static final float WELCOMING_STRANGER_SHIFT = -15f;

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
        return personality != TardisPersonality.OBEDIENT && fear(tardis) >= personality.fearToComply();
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
        if (amount > 0f && mind.has(TardisTrait.SKITTISH)) {
            amount *= SKITTISH_STRESS_GAIN;
        }
        if (amount > 0f && mind.has(TardisDisability.PTSD)) {
            amount *= PTSD_STRESS_GAIN;
        }
        if (amount > 0f && mind.trust() >= DEVOTED_TRUST) {
            amount *= DEVOTED_STRESS_GAIN;
        }
        long now = now(tardis.server());
        save(tardis, mind.withStress(mind.stressAt(now) + amount, now));
        if (amount > 0f && mind.has(TardisTrait.SKITTISH)) TardisDiscovery.trait(tardis, TardisTrait.SKITTISH);
    }

    public static void addTrust(Tardis tardis, float amount) {
        TardisMind mind = mind(tardis);
        if (mind.has(TardisTrait.LOYAL) && mind.betrayed()) {
            amount *= amount > 0f ? BETRAYED_TRUST_GAIN : BETRAYED_TRUST_LOSS;
        } else if (mind.has(TardisTrait.LOYAL)) {
            amount *= amount > 0f ? LOYAL_TRUST_GAIN : LOYAL_TRUST_LOSS;
        }
        if (mind.has(TardisTrait.DISTRUSTING)) {
            amount *= amount > 0f ? DISTRUSTING_TRUST_GAIN : DISTRUSTING_TRUST_LOSS;
        }
        save(tardis, mind.withTrust(mind.trust() + amount));
        if (mind.has(TardisTrait.LOYAL)) TardisDiscovery.trait(tardis, TardisTrait.LOYAL);
    }

    /* hurting it on purpose. distrusting ones never forget it */
    public static void abuse(Tardis tardis, float trustLoss) {
        TardisMind mind = mind(tardis);
        float loss = mind.has(TardisTrait.DISTRUSTING) ? trustLoss * DISTRUSTING_ABUSE : trustLoss;
        save(tardis, mind.withTrust(mind.trust() - loss));
    }

    /* a loyal tardis turned on. trust crashes and is very hard to win back */
    public static boolean betray(Tardis tardis) {
        TardisMind mind = mind(tardis);
        if (!mind.has(TardisTrait.LOYAL) || mind.betrayed()) return false;
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
        float shift = mind.has(TardisTrait.STUBBORN) ? STUBBORN_LIMIT_SHIFT : 0f;
        boolean stranger = pilot != null && !pilot.equals(tardis.ownerId());
        if (stranger && mind.has(TardisTrait.POSSESSIVE)) shift += POSSESSIVE_STRANGER_SHIFT;
        if (stranger && mind.has(TardisTrait.WELCOMING)) shift += WELCOMING_STRANGER_SHIFT;
        return shift;
    }

    private static void revealStrangerTraits(Tardis tardis, @Nullable UUID pilot) {
        if (pilot == null || pilot.equals(tardis.ownerId())) return;
        TardisDiscovery.trait(tardis, TardisTrait.POSSESSIVE);
        TardisDiscovery.trait(tardis, TardisTrait.WELCOMING);
    }

    public static Refusal refusesTakeoff(Tardis tardis, @Nullable UUID pilot) {
        revealStrangerTraits(tardis, pilot);
        if (!unwilling(tardis, pilot)) {
            forget(tardis);
            return Refusal.NONE;
        }
        TardisDiscovery.personality(tardis);
        TardisDiscovery.trait(tardis, TardisTrait.STUBBORN);
        if (cowed(tardis)) {
            forget(tardis);
            addStress(tardis, COWED_STRESS);
            addTrust(tardis, -COWED_TRUST_LOSS);
            return Refusal.COWED;
        }

        return switch (mind(tardis).personality()) {
            case OBEDIENT -> Refusal.NONE;
            case SUBMISSIVE -> TardisConvincing.pull(tardis);
            case DEFENSIVE -> {
                addStress(tardis, PESTER_STRESS);
                yield Refusal.REFUSE;
            }
            case AGGRESSIVE -> {
                TardisAnger.provoke(tardis, PESTER_STRESS);
                yield Refusal.REFUSE;
            }
        };
    }
}
