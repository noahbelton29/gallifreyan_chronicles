package uk.nbforge.gallifreyan_chronicles.core.item;

import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

import uk.nbforge.gallifreyan_chronicles.core.item.tooltip.ItemInfoTooltip;

public class DoorBlockItem extends BlockItem implements ScrollSelectable {
    public static List<String> doorIds() {
        return GCBlocks.pickableDoors().stream()
                .map(block -> BuiltInRegistries.BLOCK.getKey(block).getPath())
                .toList();
    }

    public DoorBlockItem(Block block, Properties properties) {
        super(block, properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.DOOR));
    }

    private String ownId() {
        return BuiltInRegistries.BLOCK.getKey(this.getBlock()).getPath();
    }

    public static String selectedId(ItemStack stack, String fallback) {
        List<String> ids = doorIds();
        String id = stack.get(GCDataComponents.DOOR_TYPE);
        if (id != null && ids.contains(id)) return id;
        return ids.contains(fallback) || ids.isEmpty() ? fallback : ids.getFirst();
    }

    public String selectedId(ItemStack stack) {
        return selectedId(stack, ownId());
    }

    @Override
    public boolean cycleSelection(ItemStack stack, int step) {
        List<String> ids = doorIds();
        if (ids.isEmpty()) return false;
        int index = Math.max(0, ids.indexOf(selectedId(stack)));
        String next = ids.get(Math.floorMod(index + step, ids.size()));
        stack.set(GCDataComponents.DOOR_TYPE, next);
        return true;
    }

    @Override
    protected @Nullable BlockState getPlacementState(BlockPlaceContext context) {
        Block chosen = BuiltInRegistries.BLOCK.getValue(GCUtil.modIdentifier(selectedId(context.getItemInHand())));
        if (!(chosen instanceof InteriorDoorBlock)) chosen = this.getBlock();
        BlockState state = chosen.getStateForPlacement(context);
        return state != null && this.canPlace(context, state) ? state : null;
    }
}
