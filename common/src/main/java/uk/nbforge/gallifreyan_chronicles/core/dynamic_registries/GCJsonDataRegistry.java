package uk.nbforge.gallifreyan_chronicles.core.dynamic_registries;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;

public class GCJsonDataRegistry<T extends IdentifiableData<T>> {
    protected final Map<String, T> byId = new HashMap<>();

    public void clear() {
        byId.clear();
    }

    public T register(T value) {
        byId.put(value.id(), value);
        return value;
    }

    public T getById(String id) {
        return byId.get(id);
    }

    public boolean contains(String id) {
        return byId.containsKey(id);
    }

    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() {
        return byId.size();
    }
}
