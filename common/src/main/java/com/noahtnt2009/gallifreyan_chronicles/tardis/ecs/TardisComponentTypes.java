package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs;

import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentType;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.*;

import java.util.List;

public final class TardisComponentTypes {
    public static final ComponentType<ExteriorComponent> EXTERIOR =
            ComponentType.persistentDefaulted("exterior", ExteriorComponent.CODEC, ExteriorComponent::ofDefault);

    public static final ComponentType<ConsoleComponent> CONSOLE =
            ComponentType.persistentDefaulted("console", ConsoleComponent.CODEC, ConsoleComponent::ofDefault);

    public static final ComponentType<DoorComponent> DOOR =
            ComponentType.persistentDefaulted("door", DoorComponent.CODEC, DoorComponent::closed);

    public static final ComponentType<TransformComponent> TRANSFORM =
            ComponentType.persistentDefaulted("transform", TransformComponent.CODEC, () -> TransformComponent.IDENTITY);

    public static final ComponentType<TardisLinkComponent> TARDIS_LINK =
            ComponentType.persistent("tardis_link", TardisLinkComponent.CODEC);

    public static final ComponentType<GlowComponent> GLOW =
            ComponentType.persistentDefaulted("glow", GlowComponent.CODEC, GlowComponent::off);

    public static final List<ComponentType<?>> ALL = List.of(EXTERIOR, CONSOLE, DOOR, TRANSFORM, TARDIS_LINK, GLOW);

    private TardisComponentTypes() {
    }

    public static void init() {
    }
}
