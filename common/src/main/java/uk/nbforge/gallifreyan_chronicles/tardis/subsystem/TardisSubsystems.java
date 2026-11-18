package uk.nbforge.gallifreyan_chronicles.tardis.subsystem;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** a subsystem with no saved health is at full health */
public final class TardisSubsystems {
    /** saved as subsystem id to health, unknown ids get dropped when loading */
    public static final Codec<Map<TardisSubsystem, Float>> HEALTH_CODEC = Codec.unboundedMap(
                    Codec.STRING, Codec.FLOAT.xmap(TardisSubsystems::clamp, health -> health))
            .xmap(
                    raw -> {
                        Map<TardisSubsystem, Float> parsed = new LinkedHashMap<>();
                        raw.forEach((key, health) -> byId(key).ifPresent(subsystem -> parsed.put(subsystem, health)));
                        return Collections.unmodifiableMap(parsed);
                    },
                    healths -> {
                        Map<String, Float> out = new HashMap<>();
                        healths.forEach((subsystem, health) -> out.put(subsystem.id(), health));
                        return out;
                    });

    private static final List<TardisSubsystem> ALL = new ArrayList<>();
    public static final LockingMechanismSubsystem LOCKING_MECHANISM = register(new LockingMechanismSubsystem());

    private TardisSubsystems() {}

    private static <T extends TardisSubsystem> T register(T subsystem) {
        ALL.add(subsystem);
        return subsystem;
    }

    public static List<TardisSubsystem> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static Optional<TardisSubsystem> byId(String id) {
        return ALL.stream().filter(subsystem -> subsystem.matches(id)).findFirst();
    }

    public static Map<TardisSubsystem, Float> fullHealth() {
        return Map.of();
    }

    public static float healthOf(Map<TardisSubsystem, Float> healths, TardisSubsystem subsystem) {
        return healths.getOrDefault(subsystem, TardisSubsystem.MAX_HEALTH);
    }

    /** returns a copy, health gets clamped between 0 and MAX_HEALTH */
    public static Map<TardisSubsystem, Float> withHealth(
            Map<TardisSubsystem, Float> healths, TardisSubsystem subsystem, float health) {
        Map<TardisSubsystem, Float> next = new LinkedHashMap<>(healths);
        next.put(subsystem, clamp(health));
        return Collections.unmodifiableMap(next);
    }

    private static float clamp(float health) {
        return Math.clamp(health, 0f, TardisSubsystem.MAX_HEALTH);
    }
}
