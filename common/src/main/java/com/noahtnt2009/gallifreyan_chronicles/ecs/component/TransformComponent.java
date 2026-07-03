package com.noahtnt2009.gallifreyan_chronicles.ecs.component;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

public record TransformComponent(float yaw) implements Component {
    public static final Codec<TransformComponent> CODEC =
            Codec.FLOAT.xmap(TransformComponent::new, TransformComponent::yaw);

    public static final TransformComponent IDENTITY = new TransformComponent(0f);
}
