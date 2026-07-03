package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class GCSounds {
    public static final SoundEvent TARDIS_DOOR_OPEN = register("tardis_door_open");
    public static final SoundEvent TARDIS_DOOR_CLOSE = register("tardis_door_close");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Constants.MOD_ID, name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void registerSounds() {}
}
