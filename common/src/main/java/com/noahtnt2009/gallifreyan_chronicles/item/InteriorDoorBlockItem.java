package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class InteriorDoorBlockItem extends BlockItem implements ScrollSelectable {
    public InteriorDoorBlockItem(Block block, Properties properties) {
        super(block, properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.INTERIOR_DOOR));
    }

    public static String selectedVariant(ItemStack stack) {
        String id = stack.get(GCDataComponents.DOOR_TYPE);
        if (id != null && TardisDoorRegistry.isInteriorVariant(id)) return TardisDoorRegistry.localId(id);
        return TardisDoorRegistry.defaultInteriorVariantId();
    }

    @Override
    public boolean cycleSelection(ItemStack stack, int step) {
        List<String> ids = TardisDoorRegistry.interiorVariantIds();
        if (ids.size() < 2) return false;
        int index = Math.max(0, ids.indexOf(selectedVariant(stack)));
        stack.set(GCDataComponents.DOOR_TYPE, ids.get(Math.floorMod(index + step, ids.size())));
        return true;
    }
}
