package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class GCSounds {
    public static final SoundEvent TARDIS_DOOR_OPEN = register("tardis_door_open");
    public static final SoundEvent TARDIS_DOOR_CLOSE = register("tardis_door_close");
    public static final SoundEvent SLIDER = register("slider_sound");
    public static final SoundEvent SWITCH = register("switch");
    public static final SoundEvent FIRST_DOCTORS_HUM = register("first_doctors_hum");
    public static final SoundEvent TARDIS_DOOR_LOCK = register("tardis_door_lock");
    public static final SoundEvent TARDIS_DOOR_UNLOCK = register("tardis_door_unlock");
    public static final SoundEvent TARDIS_IS_LOCKED = register("tardis_is_locked");
    public static final SoundEvent TARDIS_KEY_JAMMED = register("tardis_key_jammed");

    public static final float TAKEOFF_HEARING_RANGE = 25.0F;
    public static final SoundEvent TARDIS_DRUM = registerFixedRange("tardis_drum", TAKEOFF_HEARING_RANGE);
    public static final SoundEvent TARDIS_TAKEOFF = registerFixedRange("tardis_takeoff", TAKEOFF_HEARING_RANGE);
    public static final SoundEvent TARDIS_VWORP = registerFixedRange("tardis_vworp", TAKEOFF_HEARING_RANGE);


    private static SoundEvent register(String name) {
        Identifier id = GCUtils.modIdentifier(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private static SoundEvent registerFixedRange(String name, float range) {
        Identifier id = GCUtils.modIdentifier(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createFixedRangeEvent(id, range));
    }

    public static void registerSounds() {}
}
