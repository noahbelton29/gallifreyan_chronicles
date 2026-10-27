package com.noahtnt2009.gallifreyan_chronicles.item;

import net.minecraft.world.item.ItemStack;

public interface ScrollSelectable {
    boolean cycleSelection(ItemStack stack, int step);
}
