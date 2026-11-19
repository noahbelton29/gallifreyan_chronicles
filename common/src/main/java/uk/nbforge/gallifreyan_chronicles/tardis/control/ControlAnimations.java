package uk.nbforge.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ControlAnimations(String active, String inactive, boolean snapsWhenInactive) {
    public static ControlAnimations toggle(String id) {
        return toggle(id, false);
    }

    public static ControlAnimations toggle(String id, boolean snapsWhenInactive) {
        return new ControlAnimations(id + "_on", id + "_off", snapsWhenInactive);
    }

    public static ControlAnimations single(String id) {
        return new ControlAnimations(id, id, false);
    }

    public static MapCodec<ControlAnimations> codec(ControlAnimations defaults) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Codec.STRING
                                .optionalFieldOf("animation_active", defaults.active())
                                .forGetter(ControlAnimations::active),
                        Codec.STRING
                                .optionalFieldOf("animation_inactive", defaults.inactive())
                                .forGetter(ControlAnimations::inactive),
                        Codec.BOOL
                                .optionalFieldOf("snaps_when_inactive", defaults.snapsWhenInactive())
                                .forGetter(ControlAnimations::snapsWhenInactive))
                .apply(inst, ControlAnimations::new));
    }
}
