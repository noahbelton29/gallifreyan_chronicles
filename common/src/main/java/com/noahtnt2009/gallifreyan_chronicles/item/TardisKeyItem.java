package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class TardisKeyItem extends Item {
    public TardisKeyItem(Properties properties) {
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

        ItemStack stack = context.getItemInHand();
        UUID keyTardisId = stack.get(GCDataComponents.TARDIS_ID);
        UUID exteriorTardisId = exterior.getTardisId();

        if (keyTardisId == null || !keyTardisId.equals(exteriorTardisId)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        boolean inserted = Boolean.TRUE.equals(stack.get(GCDataComponents.KEY_INSERTED));

        if (!inserted) {
            if (exterior.hasKeyRendered()) return InteractionResult.FAIL;

            ItemStack toStore = stack.copyWithCount(1);
            toStore.set(GCDataComponents.KEY_INSERTED, true);
            exterior.setHeldKey(player.getUUID(), toStore);
            stack.shrink(1);
        } else {
            if (!player.getUUID().equals(exterior.getRenderedKeyOwner())) return InteractionResult.FAIL;

            ItemStack returned = exterior.clearHeldKey();
            returned.set(GCDataComponents.KEY_INSERTED, false);
            if (!player.getInventory().add(returned)) {
                player.drop(returned, false);
            }
        }

        return InteractionResult.CONSUME;
    }
}
