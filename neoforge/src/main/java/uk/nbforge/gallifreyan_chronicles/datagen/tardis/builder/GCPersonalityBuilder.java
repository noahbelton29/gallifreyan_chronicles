package uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.function.Consumer;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;

/** Fluent way to describe a TARDIS personality; every method is optional. */
public final class GCPersonalityBuilder {
    private final JsonObject json;

    public GCPersonalityBuilder(JsonObject json) {
        this.json = json;
    }

    /** How likely a new TARDIS is to roll this personality (default 10). */
    public GCPersonalityBuilder howCommon(int weight) {
        json.addProperty("how_common", weight);
        return this;
    }

    /** It refuses to fly once stress goes above the first number or trust drops below the second. */
    public GCPersonalityBuilder refusesToFlyWhen(double stressAbove, double trustBelow) {
        json.add("refuses_to_fly_when", GCJson.obj("stress_above", stressAbove, "trust_below", trustBelow));
        return this;
    }

    public GCPersonalityBuilder mallet(Consumer<Mallet> settings) {
        JsonObject mallet = new JsonObject();
        settings.accept(new Mallet(mallet));
        json.add("mallet", mallet);
        return this;
    }

    public GCPersonalityBuilder whenUnwilling(JsonObject... actions) {
        return actions("when_unwilling_to_fly", actions);
    }

    public GCPersonalityBuilder whenItSeesTheMallet(JsonObject... actions) {
        return actions("when_it_sees_the_mallet", actions);
    }

    public GCPersonalityBuilder whenHit(JsonObject... actions) {
        return actions("when_hit", actions);
    }

    public GCPersonalityBuilder whenHitWhileTerrified(JsonObject... actions) {
        return actions("when_hit_while_terrified", actions);
    }

    public GCPersonalityBuilder whenTakingOff(JsonObject... actions) {
        return actions("when_taking_off", actions);
    }

    public GCPersonalityBuilder whenLanding(JsonObject... actions) {
        return actions("when_landing", actions);
    }

    public GCPersonalityBuilder whenYouEnter(JsonObject... actions) {
        return actions("when_you_enter", actions);
    }

    private GCPersonalityBuilder actions(String key, JsonObject[] actions) {
        JsonArray list = new JsonArray();
        for (JsonObject action : actions) list.add(action);
        json.add(key, list);
        return this;
    }

    public static final class Mallet {
        private final JsonObject json;

        private Mallet(JsonObject json) {
            this.json = json;
        }

        /** Fear added by one hit. */
        public Mallet fearPerHit(double fear) {
            json.addProperty("fear_per_hit", fear);
            return this;
        }

        /** Fear added by one hit, rolled between two numbers. */
        public Mallet fearPerHit(double min, double max) {
            json.add("fear_per_hit", GCJson.obj("min", min, "max", max));
            return this;
        }

        /** How afraid it has to be before it does what it is told. */
        public Mallet fearNeededToObey(double fear) {
            json.addProperty("fear_needed_to_obey", fear);
            return this;
        }

        public Mallet traumaPerHit(double trauma) {
            json.addProperty("trauma_per_hit", trauma);
            return this;
        }

        /** How many hits it takes to snap it out of terror, rolled between two numbers. */
        public Mallet hitsToSnapOutOfTerror(int min, int max) {
            json.add("hits_to_snap_out_of_terror", GCJson.obj("min", min, "max", max));
            return this;
        }
    }
}
