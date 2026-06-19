package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public class GCSounds {
    public record SoundEntry(SoundEvent event, String subtitle) {}

    public static final List<SoundEntry> ENTRIES = new ArrayList<>();

    public static final SoundEvent POLICE_BOX_OPEN  = register("police_box_door_open",  "Police box door opens");
    public static final SoundEvent POLICE_BOX_CLOSE = register("police_box_door_close", "Police box door closes");

    private static SoundEvent register(String name, String subtitle) {
        Identifier id = Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name);
        SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        ENTRIES.add(new SoundEntry(event, subtitle));
        return event;
    }

    public static void init() {}
}