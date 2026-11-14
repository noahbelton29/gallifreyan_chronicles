package uk.nbforge.gallifreyan_chronicles.tardis.codec.ecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.ConsoleComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.DoorComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.DoorVariantComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.ExteriorComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.GlowComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.HealthComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.SubsystemsComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisLinkComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TransformComponent;

public final class EcsComponentCodec {
    public static final Codec<ConsoleComponent> CONSOLE = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("console_id").forGetter(ConsoleComponent::consoleId),
            Codec.STRING.optionalFieldOf("variant_id", "").forGetter(ConsoleComponent::variantId)
    ).apply(inst, ConsoleComponent::new));

    public static final Codec<DoorComponent> DOOR = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.xmap(DoorState::valueOf, Enum::name).fieldOf("state").forGetter(DoorComponent::state)
    ).apply(inst, state -> new DoorComponent(state, 0L)));

    public static final Codec<DoorVariantComponent> DOOR_VARIANT = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("variant_id", "").forGetter(DoorVariantComponent::variantId)
    ).apply(inst, DoorVariantComponent::new));

    public static final Codec<ExteriorComponent> EXTERIOR =
            Codec.STRING.xmap(ExteriorComponent::new, ExteriorComponent::exteriorId);

    public static final Codec<GlowComponent> GLOW = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("glowing").forGetter(GlowComponent::glowing),
            Codec.BOOL.optionalFieldOf("lamp_on", true).forGetter(GlowComponent::lampOn)
    ).apply(inst, GlowComponent::new));

    public static final Codec<HealthComponent> HEALTH =
            Codec.FLOAT.xmap(HealthComponent::new, HealthComponent::value);

    public static final Codec<SubsystemsComponent> SUBSYSTEMS =
            Codec.unboundedMap(Codec.STRING, HEALTH).xmap(
                    raw -> {
                        Map<TardisSubsystem, HealthComponent> parsed = new EnumMap<>(TardisSubsystem.class);
                        raw.forEach((key, health) -> TardisSubsystem.byId(key)
                                .ifPresent(subsystem -> parsed.put(subsystem, health)));
                        return new SubsystemsComponent(parsed);
                    },
                    component -> {
                        Map<String, HealthComponent> out = new HashMap<>();
                        component.healths().forEach((subsystem, health) -> out.put(subsystem.id(), health));
                        return out;
                    }
            );

    public static final Codec<TransformComponent> TRANSFORM =
            Codec.FLOAT.xmap(TransformComponent::new, TransformComponent::yaw);

    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<TardisComponent> TARDIS = RecordCodecBuilder.create(inst -> inst.group(
            UUID_CODEC.fieldOf("tardis_id").forGetter(TardisComponent::tardisId),
            UUID_CODEC.fieldOf("owner_id").forGetter(TardisComponent::ownerId),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(TardisComponent::ownerName)
    ).apply(inst, TardisComponent::new));

    public static final Codec<TardisLinkComponent> TARDIS_LINK =
            UUID_CODEC.xmap(TardisLinkComponent::new, TardisLinkComponent::tardisId);

    private EcsComponentCodec() {
    }
}
