package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorOptions;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class TardisBlockItem extends BlockItem implements ScrollSelectable {
    public TardisBlockItem(Block block, Properties properties) {
        super(block, properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.TARDIS));
    }

    @Override
    public boolean cycleSelection(ItemStack stack, int step) {
        List<TardisInteriorOptions.Option> options = TardisInteriorOptions.client();
        if (options.size() < 2) return false;

        String current = TardisInteriorOptions.effectiveInteriorId(stack.get(GCDataComponents.TARDIS_INTERIOR));
        int index = 0;
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).interiorId().equals(current)) {
                index = i;
                break;
            }
        }

        int next = Math.floorMod(index + step, options.size());
        stack.set(GCDataComponents.TARDIS_INTERIOR, options.get(next).interiorId());
        return true;
    }
}
