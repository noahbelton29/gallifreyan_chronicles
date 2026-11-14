package uk.nbforge.gallifreyan_chronicles.core.item;

import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.core.item.tooltip.ItemInfoTooltip;

public class MasterTardisKeyItem extends Item implements ScrollSelectable {
    @Override
    public boolean cycleSelection(ItemStack stack, int step) {
        int index = TardisKeyItem.STYLES.indexOf(TardisKeyItem.styleOf(stack.get(GCDataComponents.KEY_STYLE)));
        String next = TardisKeyItem.STYLES.get(Math.floorMod(index + step, TardisKeyItem.STYLES.size()));
        if (next.equals(TardisKeyItem.STYLES.getFirst())) {
            stack.remove(GCDataComponents.KEY_STYLE);
        } else {
            stack.set(GCDataComponents.KEY_STYLE, next);
        }
        return true;
    }

    public MasterTardisKeyItem(Properties properties) {
        super(properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.MASTER_KEY));
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof TardisExteriorBlockEntity exterior)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (exterior.hasKeyRendered()) {
            return TardisApi.of(exterior).map(tardis -> tardis.returnKeyTo(player)).orElse(false)
                    ? InteractionResult.CONSUME
                    : InteractionResult.FAIL;
        }

        if (exterior.rejectKeyIfLockBroken(player)) return InteractionResult.FAIL;

        if (!exterior.areDoorsFullyClosed()) {
            MessageUtil.overlay(player, "close_doors_to_insert_key");
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        exterior.insertKey(player, context.getHand(), stack);
        exterior.toggleKeyLockAnimation(player);

        return InteractionResult.CONSUME;
    }
}
