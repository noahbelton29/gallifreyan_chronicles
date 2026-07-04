package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

public record GlowComponent(boolean glowing, boolean manualOverride) implements Component {
    public static final Codec<GlowComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("glowing").forGetter(GlowComponent::glowing),
            Codec.BOOL.optionalFieldOf("manual_override", false).forGetter(GlowComponent::manualOverride)
    ).apply(inst, GlowComponent::new));

    public static GlowComponent off() {
        return new GlowComponent(false, false);
    }

    public GlowComponent withGlowing(boolean glowing) {
        return new GlowComponent(glowing, manualOverride);
    }

    public GlowComponent manualSet(boolean glowing) {
        return new GlowComponent(glowing, true);
    }

    public GlowComponent clearOverride() {
        return new GlowComponent(glowing, false);
    }
}
