package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;

public class GCTardisDoorVariantProvider extends GCJsonDataProvider {
    public GCTardisDoorVariantProvider(PackOutput output) {
        super(output, TardisDoorVariant.FOLDER, "TARDIS Door Variant");
    }

    @Override
    protected void addEntries() {
        // exteriorDoor takes name, block and priority, interiorDoor takes name, exterior and priority. add a default or
        // particle property for the rare extras
        exteriorDoor("first_doctors_doors", "first_doctors_door", 1).addProperty("default", true);
        exteriorDoor("hellbent_doors", "hellbent_door", 5).addProperty("particle", mod("brachacki_wall_block_4"));
        exteriorDoor("second_doctors_doors", "second_doctors_door", 2);
        exteriorDoor("second_doctors_doors_alt", "second_doctors_door_alt", 3);
        exteriorDoor("tuat_doors", "tuat_door", 4);

        interiorDoor("first_doctors_interior_doors", "first_doctors_exterior", 1)
                .addProperty("default", true);
        interiorDoor("hellbent_interior_doors", "hellbent_exterior", 4).addProperty("default", false);
        interiorDoor("second_doctors_interior_doors", "second_doctors_exterior", 2)
                .addProperty("default", false);
        interiorDoor("tuat_interior_doors", "tuat_exterior", 3).addProperty("default", false);
    }

    private JsonObject exteriorDoor(String name, String block, int priority) {
        return put(
                name,
                GCJson.obj(
                        "parent", mod("brachacki_doors"),
                        "texture", mod("brachacki_doors/" + name),
                        "open_sound", mod("hartnell_door"),
                        "close_sound", mod("hartnell_door"),
                        "open_all_only", true,
                        "block", mod(block),
                        "priority", priority));
    }

    /** model and texture share its name */
    private JsonObject interiorDoor(String name, String exterior, int priority) {
        return put(
                name,
                GCJson.obj(
                        "parent", mod(name),
                        "texture", mod(name),
                        "interior", true,
                        "exterior", mod(exterior),
                        "priority", priority));
    }

    private JsonObject put(String name, JsonObject json) {
        add(name, json);
        return json;
    }
}
