package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

import java.util.UUID;

public record TardisLinkComponent(UUID tardisId) implements Component {
    public static final Codec<TardisLinkComponent> CODEC =
            TardisComponent.UUID_CODEC.xmap(TardisLinkComponent::new, TardisLinkComponent::tardisId);
}
