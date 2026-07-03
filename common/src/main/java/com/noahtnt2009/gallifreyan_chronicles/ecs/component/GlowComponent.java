package com.noahtnt2009.gallifreyan_chronicles.ecs.component;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

public record GlowComponent(boolean glowing) implements Component {
    public static final Codec<GlowComponent> CODEC =
            Codec.BOOL.xmap(GlowComponent::new, GlowComponent::glowing);

    public static GlowComponent off() {
        return new GlowComponent(false);
    }

    public GlowComponent withGlowing(boolean glowing) {
        return new GlowComponent(glowing);
    }
}
