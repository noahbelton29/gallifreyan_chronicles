package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class GCSoundsProvider extends SoundDefinitionsProvider {
    private static final int DEFAULT_ATTENUATION = 16;
    private static final int FLIGHT_ATTENUATION = 16;
    private static final String TARDIS = "block_entity/tardis/";
    private static final String DOOR = TARDIS + "door/";
    private static final String CONTROL = TARDIS + "control/";
    private static final String ROTOR = TARDIS + "rotor/";
    private static final String TITLE = "title/";
    private static final String FX = TARDIS + "fx/";

    public GCSoundsProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    @Override
    public void registerSounds() {
        // Hum Sounds
        registerSimple(GCSounds.FIRST_DOCTORS_HUM, ROTOR, 45);

        // TARDIS Sounds
        registerSimple(GCSounds.TARDIS_DOOR_OPEN, DOOR, 30);
        registerSimple(GCSounds.TARDIS_DOOR_CLOSE, DOOR, 30);
        registerSimple(GCSounds.TARDIS_DOOR_LOCK, DOOR);
        registerSimple(GCSounds.TARDIS_DOOR_UNLOCK, DOOR);
        registerSimple(GCSounds.TARDIS_IS_LOCKED, DOOR, 25);
        registerSimple(GCSounds.TARDIS_KEY_JAMMED, DOOR);
        registerSimple(GCSounds.TARDIS_DRUM, TARDIS, FLIGHT_ATTENUATION);
        registerSimple(GCSounds.TARDIS_TAKEOFF, TARDIS, FLIGHT_ATTENUATION);
        registerSimple(GCSounds.TARDIS_LAND, TARDIS, FLIGHT_ATTENUATION);
        registerSimple(GCSounds.TARDIS_LAND_DRUM, TARDIS, FLIGHT_ATTENUATION);
        registerSimple(GCSounds.TARDIS_VWORP, TARDIS, FLIGHT_ATTENUATION);
        registerSimple(GCSounds.TARDIS_CLOISTER_BELL, TARDIS);
        registerSimple(GCSounds.TARDIS_POWER_ON, TARDIS);
        registerSimple(GCSounds.TARDIS_POWER_OFF, TARDIS);
        registerSimple(GCSounds.TARDIS_CLOAK, TARDIS);
        registerSimple(GCSounds.TARDIS_UNCLOAK, TARDIS);
        registerSimple(GCSounds.TARDIS_STOP_DEMAT, TARDIS);

        // Spark and explosion sounds
        registerVariants(GCSounds.SPARK, FX, 16, "spark_01", "spark_02");
        registerSimple(GCSounds.SPARK_EXPLOSION, FX, 24);
        registerSimple(GCSounds.EXPLOSION, FX, 32);
        // Control Sounds
        registerSimple(GCSounds.SLIDER, CONTROL);
        registerSimple(GCSounds.SWITCH, CONTROL);
        registerSimple(GCSounds.LEVER, CONTROL);

        // Door Sounds
        registerSimple(GCSounds.HARTNELL_DOOR, DOOR, 24);

        // Streamed Sounds
        registerStreamed(GCSounds.TITLE_THEME, TITLE);
    }

    private void registerSimple(SoundEvent soundEvent, String folder) {
        registerSound(soundEvent, folder, false, DEFAULT_ATTENUATION);
    }

    private void registerSimple(SoundEvent soundEvent, String folder, int attenuationDistance) {
        registerSound(soundEvent, folder, false, attenuationDistance);
    }

    /* one sound event that picks one of these files at random each time */
    private void registerVariants(SoundEvent soundEvent, String folder, int attenuationDistance, String... files) {
        SoundDefinition definition = SoundDefinition.definition()
                .subtitle("sound." + Constants.MOD_ID + "." + soundEvent.location().getPath())
                .replace(true);
        for (String file : files) {
            definition.with(sound(GCUtils.modIdentifier(folder + file), SoundDefinition.SoundType.SOUND)
                    .attenuationDistance(attenuationDistance));
        }
        add(soundEvent, definition);
    }

    private void registerStreamed(SoundEvent soundEvent, String folder) {
        registerSound(soundEvent, folder, true, DEFAULT_ATTENUATION);
    }

    private void registerSound(SoundEvent soundEvent, String folder, boolean stream, int attenuationDistance) {
        String name = soundEvent.location().getPath();
        add(soundEvent, SoundDefinition.definition()
                .with(sound(GCUtils.modIdentifier(folder + name), SoundDefinition.SoundType.SOUND)
                        .stream(stream)
                        .attenuationDistance(attenuationDistance))
                .subtitle("sound." + Constants.MOD_ID + "." + name)
                .replace(true));
    }
}
