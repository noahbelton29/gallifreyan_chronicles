package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DoorBlockItem extends BlockItem implements ScrollSelectable {
    public static final List<String> DOOR_IDS = List.of(
            "tardis_interior_door",
            "first_doctors_door",
            "second_doctors_door"
    );

    public DoorBlockItem(Block block, Properties properties) {
        super(block, properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.DOOR));
    }

    private String ownId() {
        return BuiltInRegistries.BLOCK.getKey(this.getBlock()).getPath();
    }

    public static String selectedId(ItemStack stack, String fallback) {
        String id = stack.get(GCDataComponents.DOOR_TYPE);
        return id != null && DOOR_IDS.contains(id) ? id : fallback;
    }

    public String selectedId(ItemStack stack) {
        return selectedId(stack, ownId());
    }

    @Override
    public boolean cycleSelection(ItemStack stack, int step) {
        int index = Math.max(0, DOOR_IDS.indexOf(selectedId(stack)));
        String next = DOOR_IDS.get(Math.floorMod(index + step, DOOR_IDS.size()));
        stack.set(GCDataComponents.DOOR_TYPE, next);
        return true;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        Block chosen = BuiltInRegistries.BLOCK.getValue(GCUtils.modIdentifier(selectedId(context.getItemInHand())));
        if (!(chosen instanceof InteriorDoorBlock)) chosen = this.getBlock();
        BlockState state = chosen.getStateForPlacement(context);
        return state != null && this.canPlace(context, state) ? state : null;
    }
}
