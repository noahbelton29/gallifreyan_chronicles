package com.noahtnt2009.gallifreyan_chronicles.ecs;

public final class Entity {
    private final ComponentStore store;
    private final Runnable onDirty;

    private Entity(ComponentStore store, Runnable onDirty) {
        this.store = store;
        this.onDirty = onDirty;
    }

    public static Entity of(ComponentHolder target, Runnable onDirty) {
        return new Entity(target.componentStore(), onDirty);
    }

    public boolean has(ComponentType<?> type) {
        return store.has(type);
    }

    public <T extends Component> T get(ComponentType<T> type) {
        return store.getOrCreate(type);
    }

    public <T extends Component> T getOrNull(ComponentType<T> type) {
        return store.getOrNull(type);
    }
    public <T extends Component> void set(ComponentType<T> type, T value) {
        store.set(type, value);
        onDirty.run();
    }

    public void markChanged() {
        onDirty.run();
    }
}
