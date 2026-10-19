package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class MasterTardisKeyItem extends Item {
    public MasterTardisKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        if (!(level.getBlockEntity(TardisTopBlock.baseOf(level, context.getClickedPos())) instanceof TardisExteriorBlockEntity exterior)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (exterior.hasKeyRendered()) {
            ItemStack returned = exterior.clearHeldKey();
            returned.set(GCDataComponents.KEY_INSERTED, false);
            if (!player.getInventory().add(returned)) {
                player.drop(returned, false, Prediction.SERVER_ONLY);
            }
            return InteractionResult.CONSUME;
        }

        if (!exterior.areDoorsFullyClosed()) {
            player.sendOverlayMessage(Component.literal("Close both doors before inserting the key!"));
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        ItemStack toStore = stack.copyWithCount(1);
        toStore.set(GCDataComponents.KEY_INSERTED, true);
        exterior.setHeldKey(player.getUUID(), toStore);
        stack.shrink(1);
        exterior.toggleKeyLockAnimation();

        return InteractionResult.CONSUME;
    }
}
