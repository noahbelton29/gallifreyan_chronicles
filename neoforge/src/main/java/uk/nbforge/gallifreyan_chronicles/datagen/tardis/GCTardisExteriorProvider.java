package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;

public class GCTardisExteriorProvider extends GCJsonDataProvider {
    public GCTardisExteriorProvider(PackOutput output) {
        super(output, "tardis_exterior", "TARDIS Exterior");
    }

    @Override
    protected void addEntries() {
        exterior("first_doctors_exterior");
        exterior("hellbent_exterior");
        exterior("second_doctors_exterior", "second_doctors_tardis");
        exterior("tuat_exterior", "tuat_tardis");
    }

    private void exterior(String name) {
        add(name, GCJson.obj());
    }

    /** waypointStyle is the locator bar icon in assets/waypoint_style, "tardis" when left out. */
    private void exterior(String name, String waypointStyle) {
        add(name, GCJson.obj("waypoint_style", waypointStyle));
    }
}
