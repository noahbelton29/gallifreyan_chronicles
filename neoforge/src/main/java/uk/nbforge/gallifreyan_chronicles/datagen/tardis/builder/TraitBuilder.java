package uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Locale;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;

/**
 * Every method is optional.
 * Multipliers called times default to 1 and shifts called by default to 0 so only set what the trait changes.
 */
public final class TraitBuilder {
    private final JsonObject json;

    public TraitBuilder(JsonObject json) {
        this.json = json;
    }

    /** defaults to 10 */
    public TraitBuilder howCommon(int weight) {
        json.addProperty("how_common", weight);
        return this;
    }

    public TraitBuilder stressBuildsUp(double times) {
        return set("stress", "builds_up", times);
    }

    public TraitBuilder stressCalmsDown(double times) {
        return set("stress", "calms_down", times);
    }

    public TraitBuilder fearFades(double times) {
        return set("fear", "fades", times);
    }

    public TraitBuilder trustGained(double times) {
        return set("trust", "gained", times);
    }

    public TraitBuilder trustLost(double times) {
        return set("trust", "lost", times);
    }

    public TraitBuilder trustLostWhenAbused(double times) {
        return set("trust", "lost_when_abused", times);
    }

    public TraitBuilder startsWithTrust(double trust) {
        return set("trust", "starts_at", trust);
    }

    public TraitBuilder strangersStartWithTrust(double trust) {
        return set("trust", "strangers_start_at", trust);
    }

    public TraitBuilder traumaTaken(double times) {
        json.addProperty("trauma_taken", times);
        return this;
    }

    public TraitBuilder crashDamage(double times) {
        json.addProperty("crash_damage", times);
        return this;
    }

    public TraitBuilder refusesSoonerBy(double amount) {
        json.addProperty("refuses_sooner_by", amount);
        return this;
    }

    public TraitBuilder refusesStrangersSoonerBy(double amount) {
        json.addProperty("refuses_strangers_sooner_by", amount);
        return this;
    }

    public TraitBuilder ifBetrayed(double trustGainedTimes, double trustLostTimes, double traumaTakenTimes) {
        json.add(
                "if_betrayed",
                GCJson.obj(
                        "trust_gained",
                        trustGainedTimes,
                        "trust_lost",
                        trustLostTimes,
                        "trauma_taken",
                        traumaTakenTimes));
        return this;
    }

    public TraitBuilder neverWithTraits(String... traits) {
        return never("traits", traits);
    }

    public TraitBuilder neverWithPersonalities(String... personalities) {
        return never("personalities", personalities);
    }

    public TraitBuilder revealedWhen(Reveal reveal) {
        json.addProperty("revealed_when", reveal.name().toLowerCase(Locale.ROOT));
        return this;
    }

    public TraitBuilder whenUnwilling(JsonObject... actions) {
        return actions("when_unwilling_to_fly", actions);
    }

    public TraitBuilder whenTakingOff(JsonObject... actions) {
        return actions("when_taking_off", actions);
    }

    public TraitBuilder whenLanding(JsonObject... actions) {
        return actions("when_landing", actions);
    }

    private TraitBuilder set(String section, String key, double value) {
        if (!json.has(section)) json.add(section, new JsonObject());
        json.getAsJsonObject(section).addProperty(key, value);
        return this;
    }

    private TraitBuilder never(String key, String[] ids) {
        if (!json.has("never_with")) json.add("never_with", new JsonObject());
        JsonArray list = new JsonArray();
        for (String id : ids) list.add(id);
        json.getAsJsonObject("never_with").add(key, list);
        return this;
    }

    private TraitBuilder actions(String key, JsonObject[] actions) {
        JsonArray list = new JsonArray();
        for (JsonObject action : actions) list.add(action);
        json.add(key, list);
        return this;
    }
}
