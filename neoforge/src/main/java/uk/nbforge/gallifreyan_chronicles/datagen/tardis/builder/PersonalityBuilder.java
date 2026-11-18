package uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;

public final class PersonalityBuilder {
    private final JsonObject json;

    public PersonalityBuilder(JsonObject json) {
        this.json = json;
    }

    /** defaults to 10 */
    public PersonalityBuilder howCommon(int weight) {
        json.addProperty("how_common", weight);
        return this;
    }

    /** It refuses to fly once stress goes above the first number or trust drops below the second. */
    public PersonalityBuilder refusesToFlyWhen(double stressAbove, double trustBelow) {
        json.add("refuses_to_fly_when", GCJson.obj("stress_above", stressAbove, "trust_below", trustBelow));
        return this;
    }

    public PersonalityBuilder mallet(Consumer<Mallet> settings) {
        JsonObject mallet = new JsonObject();
        settings.accept(new Mallet(mallet));
        json.add("mallet", mallet);
        return this;
    }

    public PersonalityBuilder whenUnwilling(JsonObject... actions) {
        return actions("when_unwilling_to_fly", actions);
    }

    public PersonalityBuilder whenItSeesTheMallet(JsonObject... actions) {
        return actions("when_it_sees_the_mallet", actions);
    }

    public PersonalityBuilder whenHit(JsonObject... actions) {
        return actions("when_hit", actions);
    }

    public PersonalityBuilder whenHitWhileTerrified(JsonObject... actions) {
        return actions("when_hit_while_terrified", actions);
    }

    public PersonalityBuilder whenTakingOff(JsonObject... actions) {
        return actions("when_taking_off", actions);
    }

    public PersonalityBuilder whenLanding(JsonObject... actions) {
        return actions("when_landing", actions);
    }

    public PersonalityBuilder whenYouEnter(JsonObject... actions) {
        return actions("when_you_enter", actions);
    }

    private PersonalityBuilder actions(String key, JsonObject[] actions) {
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

        public Mallet fearPerHit(double fear) {
            json.addProperty("fear_per_hit", fear);
            return this;
        }

        public Mallet fearPerHit(double min, double max) {
            json.add("fear_per_hit", GCJson.obj("min", min, "max", max));
            return this;
        }

        public Mallet fearNeededToObey(double fear) {
            json.addProperty("fear_needed_to_obey", fear);
            return this;
        }

        public Mallet traumaPerHit(double trauma) {
            json.addProperty("trauma_per_hit", trauma);
            return this;
        }

        public Mallet hitsToSnapOutOfTerror(int min, int max) {
            json.add("hits_to_snap_out_of_terror", GCJson.obj("min", min, "max", max));
            return this;
        }
    }
}
