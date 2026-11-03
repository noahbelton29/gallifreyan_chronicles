package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

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
        super(properties);
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
            player.sendOverlayMessage(Component.translatable("message.gallifreyan_chronicles.close_doors_to_insert_key"));
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        exterior.insertKey(player, context.getHand(), stack);
        exterior.toggleKeyLockAnimation(player);

        return InteractionResult.CONSUME;
    }
}
