package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import com.google.gson.JsonObject;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder.GCConsoleBuilder;

/**
 * Generates data/gallifreyan_chronicles/tardis_console/*.json.
 * To add a console, copy a block below. {@code standardControls()} adds every normal control with its usual
 * cooldown; use {@code adjust(...)} to place or resize one for this console's model, and the ids in
 * {@link GCTardisControls} for anything else.
 */
public class GCTardisConsoleProvider extends GCJsonDataProvider {
    public GCTardisConsoleProvider(PackOutput output) {
        super(output, "tardis_console", "TARDIS Console");
    }

    @Override
    protected void addEntries() {
        console("ambassadors_of_death_console")
                .standardGlowGroups()
                .standardControls()
                .adjust(GCTardisControls.CLOAK_CONTROL, c -> c.at(-0.525, 0.6875, -0.5).size(0.0625, 0.1875, 0.0625))
                .adjust(GCTardisControls.ISOMORPHIC_CONTROL, c -> c.at(-0.0312, 0.5592, 1.0224).size(0.25, 0.125, 0.125))
                .adjust(GCTardisControls.DIRECTION_CONTROL, c -> c.at(0.7715, 0.6968, 0.168).size(0.125, 0.1875, 0.1875));

        console("brachacki_console")
                .defaultVariant("first_doctors_console")
                .standardGlowGroups()
                .standardControls()
                .interiorDoorControl();

        console("inferno_console")
                .standardGlowGroups()
                .standardControls()
                .adjust(GCTardisControls.CLOAK_CONTROL, c -> c.at(-0.525, 0.6875, -0.5).size(0.0625, 0.1875, 0.0625))
                .adjust(GCTardisControls.ISOMORPHIC_CONTROL, c -> c.at(-0.0312, 0.5592, 1.0224).size(0.25, 0.125, 0.125))
                .adjust(GCTardisControls.DIRECTION_CONTROL, c -> c.at(0.7715, 0.6968, 0.168).size(0.125, 0.1875, 0.1875));

        console("second_doctors_console_alt")
                .defaultVariant("second_doctors_console_alt")
                .standardGlowGroups()
                .standardControls()
                .interiorDoorControl(c -> c.at(1.0237, 0.5653, 0.388).size(0.125, 0.125, 0.1875));
    }

    private GCConsoleBuilder console(String name) {
        JsonObject json = new JsonObject();
        add(name, json);
        return new GCConsoleBuilder(json);
    }
}
