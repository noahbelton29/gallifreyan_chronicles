package com.noahtnt2009.gallifreyan_chronicles.ecs.component;

import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentType;

import java.util.List;

public final class ComponentTypes {
    public static final ComponentType<ExteriorComponent> EXTERIOR =
            ComponentType.persistentDefaulted("exterior", ExteriorComponent.CODEC, ExteriorComponent::ofDefault);

    public static final ComponentType<DoorComponent> DOOR =
            ComponentType.persistentDefaulted("door", DoorComponent.CODEC, DoorComponent::closed);

    public static final ComponentType<TransformComponent> TRANSFORM =
            ComponentType.persistentDefaulted("transform", TransformComponent.CODEC, () -> TransformComponent.IDENTITY);

    public static final ComponentType<TardisLinkComponent> TARDIS_LINK =
            ComponentType.persistent("tardis_link", TardisLinkComponent.CODEC);

    public static final ComponentType<GlowComponent> GLOW =
            ComponentType.persistentDefaulted("glow", GlowComponent.CODEC, GlowComponent::off);

    public static final List<ComponentType<?>> ALL = List.of(EXTERIOR, DOOR, TRANSFORM, TARDIS_LINK, GLOW);

    private ComponentTypes() {
    }

    public static void init() {
    }
}
