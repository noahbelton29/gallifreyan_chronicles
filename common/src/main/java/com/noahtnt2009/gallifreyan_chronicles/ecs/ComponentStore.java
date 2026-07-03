package com.noahtnt2009.gallifreyan_chronicles.ecs;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;

public final class ComponentStore {
    private final Map<ComponentType<?>, Object> data = new HashMap<>();

    public boolean has(ComponentType<?> type) {
        return data.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getOrCreate(ComponentType<T> type) {
        return (T) data.computeIfAbsent(type, k -> {
            T def = type.createDefault();
            if (def == null) throw new IllegalStateException(
                    "Component " + type.id() + " has no default and has not been set");
            return def;
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getOrNull(ComponentType<T> type) {
        return (T) data.get(type);
    }

    public <T extends Component> void set(ComponentType<T> type, T value) {
        data.put(type, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void save(ValueOutput output, Iterable<ComponentType<?>> knownTypes) {
        for (ComponentType<?> type : knownTypes) {
            if (!type.isPersistent()) continue;
            Object value = data.get(type);
            if (value == null) continue;
            output.store(type.id(), ((ComponentType) type).codec(), value);
        }
    }

    public void load(ValueInput input, Iterable<ComponentType<?>> knownTypes) {
        for (ComponentType<?> type : knownTypes) {
            if (!type.isPersistent()) continue;
            loadEntry(input, type);
        }
    }

    private <T extends Component> void loadEntry(ValueInput input, ComponentType<T> type) {
        input.read(type.id(), type.codec()).ifPresent(value -> data.put(type, value));
    }
}