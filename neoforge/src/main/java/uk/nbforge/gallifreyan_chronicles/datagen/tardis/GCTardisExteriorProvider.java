package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;

/** Generates data/gallifreyan_chronicles/tardis_exterior/*.json. To add one, copy a line below and change the names. */
public class GCTardisExteriorProvider extends GCJsonDataProvider {
    public GCTardisExteriorProvider(PackOutput output) {
        super(output, "tardis_exterior", "TARDIS Exterior");
    }

    @Override
    protected void addEntries() {
        exterior("first_doctors_exterior");
        exterior("hellbent_exterior");
        exterior("second_doctors_exterior");
        exterior("tuat_exterior");
    }

    /** An exterior is just its model; the file only has to exist. */
    private void exterior(String name) {
        add(name, GCJson.obj());
    }
}
