package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import com.google.gson.JsonObject;

import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;

/** Generates data/gallifreyan_chronicles/tardis_console_variant/*.json. To add one, copy a line below and change the names. */
public class GCTardisConsoleVariantProvider extends GCJsonDataProvider {
    public GCTardisConsoleVariantProvider(PackOutput output) {
        super(output, "tardis_console_variant", "TARDIS Console Variant");
    }

    @Override
    protected void addEntries() {
        // consoleVariant(name, console model, texture, glowing parts...)
        consoleVariant("ambassadors_of_death_console", "ambassadors_of_death_console", "ambassadors_of_death_console", "buttons");
        consoleVariant("first_doctors_console", "brachacki_console", "brachacki_console/first_doctors_console", "lights", "rotor");
        consoleVariant("inferno_console", "inferno_console", "inferno_console", "buttons", "rotor");
        consoleVariant("second_doctors_console", "brachacki_console", "brachacki_console/second_doctors_console", "buttons", "lights", "rotor");
        consoleVariant("second_doctors_console_alt", "second_doctors_console_alt", "second_doctors_console_alt", "buttons", "lights", "rotor");
        consoleVariant("tuat_console", "brachacki_console", "brachacki_console/tuat_console", "buttons", "lights", "rotor");
    }

    /** Each glow part uses the shared standard glow texture. */
    private void consoleVariant(String name, String console, String texture, String... glowParts) {
        JsonObject glow = new JsonObject();
        for (String part : glowParts) glow.addProperty(part, mod("console_shared/standard"));
        add(name, GCJson.obj("parent", mod(console), "texture", mod(texture), "glow_textures", glow));
    }
}
