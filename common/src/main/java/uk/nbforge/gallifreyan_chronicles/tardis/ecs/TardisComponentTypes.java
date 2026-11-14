package uk.nbforge.gallifreyan_chronicles.tardis.ecs;

import uk.nbforge.gallifreyan_chronicles.ecs.ComponentType;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.*;

import java.util.List;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.ecs.EcsComponentCodec;

public final class TardisComponentTypes {
    public static final ComponentType<ExteriorComponent> EXTERIOR =
            ComponentType.persistentDefaulted("exterior", EcsComponentCodec.EXTERIOR, ExteriorComponent::ofDefault);

    public static final ComponentType<ConsoleComponent> CONSOLE =
            ComponentType.persistentDefaulted("console", EcsComponentCodec.CONSOLE, ConsoleComponent::ofDefault);

    public static final ComponentType<DoorComponent> DOOR =
            ComponentType.persistentDefaulted("door", EcsComponentCodec.DOOR, DoorComponent::closed);

    public static final ComponentType<DoorVariantComponent> DOOR_VARIANT =
            ComponentType.persistentDefaulted("door_variant", EcsComponentCodec.DOOR_VARIANT, DoorVariantComponent::unset);

    public static final ComponentType<TransformComponent> TRANSFORM =
            ComponentType.persistentDefaulted("transform", EcsComponentCodec.TRANSFORM, () -> TransformComponent.IDENTITY);

    public static final ComponentType<TardisLinkComponent> TARDIS_LINK =
            ComponentType.persistent("tardis_link", EcsComponentCodec.TARDIS_LINK);

    public static final ComponentType<GlowComponent> GLOW =
            ComponentType.persistentDefaulted("glow", EcsComponentCodec.GLOW, GlowComponent::fresh);

    public static final ComponentType<SubsystemsComponent> SUBSYSTEMS =
            ComponentType.persistentDefaulted("subsystems", EcsComponentCodec.SUBSYSTEMS, SubsystemsComponent::fresh);

    public static final List<ComponentType<?>> ALL =
            List.of(EXTERIOR, CONSOLE, DOOR, TRANSFORM, TARDIS_LINK, GLOW, SUBSYSTEMS);

    private TardisComponentTypes() {
    }

    public static void init() {
    }
}
