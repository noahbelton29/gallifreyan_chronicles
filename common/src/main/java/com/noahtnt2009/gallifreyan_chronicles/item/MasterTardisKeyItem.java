package com.noahtnt2009.gallifreyan_chronicles.item;

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

public class MasterTardisKeyItem extends Item {
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
            player.sendOverlayMessage(Component.literal("Close both doors before inserting the key!"));
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        exterior.insertKey(player, context.getHand(), stack);
        exterior.toggleKeyLockAnimation(player);

        return InteractionResult.CONSUME;
    }
}
