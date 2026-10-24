package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils; import net.minecraft.core.Registry; import net.minecraft.core.registries.BuiltInRegistries; import net.minecraft.resources.Identifier; import net.minecraft.sounds.SoundEvent;

public class GCSounds {
    public static final float HEARING_RANGE = 10.0F;
    public static final SoundEvent TARDIS_DOOR_OPEN = registerFixedRange("tardis_door_open", HEARING_RANGE);
    public static final SoundEvent TARDIS_DOOR_CLOSE = registerFixedRange("tardis_door_close", HEARING_RANGE);
    public static final SoundEvent SLIDER = registerFixedRange("slider_sound", HEARING_RANGE);
    public static final SoundEvent SWITCH = registerFixedRange("switch", HEARING_RANGE);
    public static final SoundEvent FIRST_DOCTORS_HUM = registerFixedRange("first_doctors_hum", HEARING_RANGE);
    public static final SoundEvent TARDIS_DOOR_LOCK = registerFixedRange("tardis_door_lock", HEARING_RANGE);
    public static final SoundEvent TARDIS_DOOR_UNLOCK = registerFixedRange("tardis_door_unlock", HEARING_RANGE);
    public static final SoundEvent TARDIS_IS_LOCKED = registerFixedRange("tardis_is_locked", HEARING_RANGE);
    public static final SoundEvent TARDIS_KEY_JAMMED = registerFixedRange("tardis_key_jammed", HEARING_RANGE);

    public static final float TAKEOFF_HEARING_RANGE = 25.0F;

    public static final SoundEvent TARDIS_DRUM =
            registerFixedRange("tardis_drum", TAKEOFF_HEARING_RANGE);
    public static final SoundEvent TARDIS_LAND =
            registerFixedRange("tardis_land", TAKEOFF_HEARING_RANGE);
    public static final SoundEvent TARDIS_LAND_DRUM =
            registerFixedRange("tardis_land_drum", TAKEOFF_HEARING_RANGE);
    public static final SoundEvent TARDIS_TAKEOFF =
            registerFixedRange("tardis_takeoff", TAKEOFF_HEARING_RANGE);
    public static final SoundEvent TARDIS_VWORP =
            registerFixedRange("tardis_vworp", TAKEOFF_HEARING_RANGE);

    private static SoundEvent register(String name) {
        Identifier id = GCUtils.modIdentifier(name);
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id)
        );
    }

    private static SoundEvent registerFixedRange(String name, float range) {
        Identifier id = GCUtils.modIdentifier(name);
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createFixedRangeEvent(id, range)
        );
    }

    public static void registerSounds() {}


}