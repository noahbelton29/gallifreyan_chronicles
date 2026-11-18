package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;

public class GCTardisInteriorProvider extends GCJsonDataProvider {
    public GCTardisInteriorProvider(PackOutput output) {
        super(output, "tardis_interior", "TARDIS Interior");
    }

    @Override
    protected void addEntries() {
        interior("first_doctors_interior", "first_doctors_exterior");
        interior("hellbent_interior", "hellbent_exterior");
        interior("second_doctors_interior", "second_doctors_exterior");
        interior("second_doctors_interior_alt", "second_doctors_exterior");
        interior("tuat_interior", "tuat_exterior");
        add("template_interior", GCJson.obj());
    }

    private void interior(String name, String exterior) {
        add(name, GCJson.obj("exterior", exterior));
    }
}
