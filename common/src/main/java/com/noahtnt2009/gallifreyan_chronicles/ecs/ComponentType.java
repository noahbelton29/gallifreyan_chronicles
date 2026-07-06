package com.noahtnt2009.gallifreyan_chronicles.ecs;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;

import java.util.function.Supplier;

public final class ComponentType<T extends Component> {
    private final String id;
    private final Codec<T> codec;
    private final Supplier<T> initializer;

    private ComponentType(String id, Codec<T> codec, Supplier<T> initializer) {
        this.id = GCUtils.of(id).toString();
        this.codec = codec;
        this.initializer = initializer;
    }

    public static <T extends Component> ComponentType<T> transientDefaulted(String id, Supplier<T> initializer) {
        return new ComponentType<>(id, null, initializer);
    }

    public static <T extends Component> ComponentType<T> persistentDefaulted(
            String id, Codec<T> codec, Supplier<T> initializer) {
        return new ComponentType<>(id, codec, initializer);
    }

    public static <T extends Component> ComponentType<T> persistent(String id, Codec<T> codec) {
        return new ComponentType<>(id, codec, null);
    }

    public String id() {
        return id;
    }
    public boolean isPersistent() {
        return codec != null;
    }
    public Codec<T> codec() {
        return codec;
    }
    public T createDefault() {
        return initializer != null ? initializer.get() : null;
    }
}
