package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ControlAnimations(String active, String inactive, boolean snapsWhenInactive) {
    public static MapCodec<ControlAnimations> codec(ControlAnimations defaults) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.optionalFieldOf("animation_active", defaults.active())
                        .forGetter(ControlAnimations::active),
                Codec.STRING.optionalFieldOf("animation_inactive", defaults.inactive())
                        .forGetter(ControlAnimations::inactive),
                Codec.BOOL.optionalFieldOf("snaps_when_inactive", defaults.snapsWhenInactive())
                        .forGetter(ControlAnimations::snapsWhenInactive)
        ).apply(inst, ControlAnimations::new));
    }
}
