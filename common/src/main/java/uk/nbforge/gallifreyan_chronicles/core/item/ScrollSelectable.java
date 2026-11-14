package uk.nbforge.gallifreyan_chronicles.core.item;

import net.minecraft.world.item.ItemStack;

public interface ScrollSelectable {
    boolean cycleSelection(ItemStack stack, int step);
}
