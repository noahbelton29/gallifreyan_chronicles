package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

/* glowing: is the lamp lit right now. lampOn: is it switched on (on by default, it only lights with power) */
public record GlowComponent(boolean glowing, boolean lampOn) implements Component {
    public static final Codec<GlowComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("glowing").forGetter(GlowComponent::glowing),
            Codec.BOOL.optionalFieldOf("lamp_on", true).forGetter(GlowComponent::lampOn)
    ).apply(inst, GlowComponent::new));

    public static GlowComponent fresh() {
        return new GlowComponent(false, true);
    }

    public GlowComponent withGlowing(boolean glowing) {
        return new GlowComponent(glowing, lampOn);
    }

    public GlowComponent withLampOn(boolean lampOn) {
        return new GlowComponent(glowing, lampOn);
    }
}
