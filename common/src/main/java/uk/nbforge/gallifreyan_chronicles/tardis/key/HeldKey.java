package uk.nbforge.gallifreyan_chronicles.tardis.key;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public final class HeldKey {
    private @Nullable UUID owner;
    private ItemStack stack = ItemStack.EMPTY;

    public @Nullable UUID owner() {
        return owner;
    }

    public ItemStack stack() {
        return stack;
    }

    public boolean isPresent() {
        return owner != null;
    }

    public void set(UUID owner, ItemStack stack) {
        this.owner = owner;
        this.stack = stack.copy();
    }

    public ItemStack clear() {
        ItemStack returned = stack.copy();
        owner = null;
        stack = ItemStack.EMPTY;
        return returned;
    }

    public void copyFrom(HeldKey other) {
        this.owner = other.owner;
        this.stack = other.stack.copy();
    }

    public void save(ValueOutput output) {
        if (owner != null) {
            output.putString("RenderedKeyOwner", owner.toString());
            output.store("HeldKeyStack", ItemStack.OPTIONAL_CODEC, stack);
        }
    }

    public void load(ValueInput input) {
        input.getString("RenderedKeyOwner").ifPresent(s -> owner = UUID.fromString(s));
        stack = input.read("HeldKeyStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
