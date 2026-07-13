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
    public static final SoundEvent FIRST_DOCTORS_HUM = register("first_doctors_hum");
    public static final SoundEvent TARDIS_DOOR_LOCK = register("tardis_door_lock");
    public static final SoundEvent TARDIS_DOOR_UNLOCK = register("tardis_door_unlock");
    public static final SoundEvent TARDIS_IS_LOCKED = register("tardis_is_locked");


    private static SoundEvent register(String name) {
        Identifier id = GCUtils.of(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerSounds() {}
}
