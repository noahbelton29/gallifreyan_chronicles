package uk.nbforge.gallifreyan_chronicles.util;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * stays unset until its read or written and only a set value gets saved. loading only replaces it when the key is in
 * the data, so a sync packet without the key leaves the current value alone
 */
public final class SavedValue<T> {
    private final String key;
    private final Codec<T> codec;
    private final @Nullable Supplier<T> fallback;
    private @Nullable T value;

    public SavedValue(String name, Codec<T> codec, @Nullable Supplier<T> fallback) {
        this.key = GCUtil.modIdentifier(name).toString();
        this.codec = codec;
        this.fallback = fallback;
    }

    /** no default, read it with getOrNull */
    public SavedValue(String name, Codec<T> codec) {
        this(name, codec, null);
    }

    public T get() {
        if (value == null) {
            T made = fallback == null ? null : fallback.get();
            if (made == null) throw new IllegalStateException("Value " + key + " has no default and has not been set");
            value = made;
        }
        return value;
    }

    public @Nullable T getOrNull() {
        return value;
    }

    public void set(@Nullable T value) {
        this.value = value;
    }

    public void copyFrom(SavedValue<T> other) {
        if (other.value != null) value = other.value;
    }

    public void save(ValueOutput output) {
        if (value != null) output.store(key, codec, value);
    }

    public void load(ValueInput input) {
        input.read(key, codec).ifPresent(loaded -> value = loaded);
    }
}
