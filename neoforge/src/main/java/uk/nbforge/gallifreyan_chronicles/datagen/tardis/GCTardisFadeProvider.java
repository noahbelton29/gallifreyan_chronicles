package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJson;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;

/** Generates data/gallifreyan_chronicles/tardis_fade/*.json. To add one, copy a line below and change the names. */
public class GCTardisFadeProvider extends GCJsonDataProvider {
    public GCTardisFadeProvider(PackOutput output) {
        super(output, "tardis_fade", "TARDIS Fade");
    }

    @Override
    protected void addEntries() {
        fade("classicwho");
        pulse("newwho");
        // name, pulse_ticks, pulse_ramp, pulse_depth, takeoff_delay_ticks, fade_out_ticks, fade_in_ticks
        pulse("slow_pulse", 60, 1.5, 0.7, 40, 300, 360);
    }

    private void fade(String name) {
        add(name, GCJson.obj("type", "fade"));
    }

    private void pulse(String name) {
        add(name, GCJson.obj("type", "pulse"));
    }

    private void pulse(String name, int pulseTicks, double ramp, double depth, int takeoffDelay, int fadeOut, int fadeIn) {
        add(name, GCJson.obj("type", "pulse", "pulse_ticks", pulseTicks, "pulse_ramp", ramp, "pulse_depth", depth,
                "takeoff_delay_ticks", takeoffDelay, "fade_out_ticks", fadeOut, "fade_in_ticks", fadeIn));
    }
}
