package uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;

import java.util.Locale;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;

/**
 * Fluent way to describe a TARDIS trait; every method is optional. Multipliers ("times") default to 1,
 * shifts ("by") to 0, so only set what the trait changes.
 */
public final class GCTraitBuilder {
    private final JsonObject json;

    public GCTraitBuilder(JsonObject json) {
        this.json = json;
    }

    /** How likely a trait is to be picked (default 10). */
    public GCTraitBuilder howCommon(int weight) {
        json.addProperty("how_common", weight);
        return this;
    }

    // stress
    public GCTraitBuilder stressBuildsUp(double times) {
        return set("stress", "builds_up", times);
    }

    public GCTraitBuilder stressCalmsDown(double times) {
        return set("stress", "calms_down", times);
    }

    // fear
    public GCTraitBuilder fearFades(double times) {
        return set("fear", "fades", times);
    }

    // trust
    public GCTraitBuilder trustGained(double times) {
        return set("trust", "gained", times);
    }

    public GCTraitBuilder trustLost(double times) {
        return set("trust", "lost", times);
    }

    public GCTraitBuilder trustLostWhenAbused(double times) {
        return set("trust", "lost_when_abused", times);
    }

    public GCTraitBuilder startsWithTrust(double trust) {
        return set("trust", "starts_at", trust);
    }

    public GCTraitBuilder strangersStartWithTrust(double trust) {
        return set("trust", "strangers_start_at", trust);
    }

    // everything else
    public GCTraitBuilder traumaTaken(double times) {
        json.addProperty("trauma_taken", times);
        return this;
    }

    public GCTraitBuilder crashDamage(double times) {
        json.addProperty("crash_damage", times);
        return this;
    }

    public GCTraitBuilder refusesSoonerBy(double amount) {
        json.addProperty("refuses_sooner_by", amount);
        return this;
    }

    public GCTraitBuilder refusesStrangersSoonerBy(double amount) {
        json.addProperty("refuses_strangers_sooner_by", amount);
        return this;
    }

    /** Different multipliers once the player has betrayed it. */
    public GCTraitBuilder ifBetrayed(double trustGainedTimes, double trustLostTimes, double traumaTakenTimes) {
        json.add("if_betrayed", GCJson.obj("trust_gained", trustGainedTimes, "trust_lost", trustLostTimes,
                "trauma_taken", traumaTakenTimes));
        return this;
    }

    // who it can't be rolled with
    public GCTraitBuilder neverWithTraits(String... traits) {
        return never("traits", traits);
    }

    public GCTraitBuilder neverWithPersonalities(String... personalities) {
        return never("personalities", personalities);
    }

    /** What first makes the player notice this trait. */
    public GCTraitBuilder revealedWhen(Reveal reveal) {
        json.addProperty("revealed_when", reveal.name().toLowerCase(Locale.ROOT));
        return this;
    }

    public GCTraitBuilder whenUnwilling(JsonObject... actions) {
        return actions("when_unwilling_to_fly", actions);
    }

    public GCTraitBuilder whenTakingOff(JsonObject... actions) {
        return actions("when_taking_off", actions);
    }

    public GCTraitBuilder whenLanding(JsonObject... actions) {
        return actions("when_landing", actions);
    }

    private GCTraitBuilder set(String section, String key, double value) {
        if (!json.has(section)) json.add(section, new JsonObject());
        json.getAsJsonObject(section).addProperty(key, value);
        return this;
    }

    private GCTraitBuilder never(String key, String[] ids) {
        if (!json.has("never_with")) json.add("never_with", new JsonObject());
        JsonArray list = new JsonArray();
        for (String id : ids) list.add(id);
        json.getAsJsonObject("never_with").add(key, list);
        return this;
    }

    private GCTraitBuilder actions(String key, JsonObject[] actions) {
        JsonArray list = new JsonArray();
        for (JsonObject action : actions) list.add(action);
        json.add(key, list);
        return this;
    }
}
