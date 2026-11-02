package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class TardisKeyItem extends Item {
    private static InteractionResult linkToConsole(Player player, InteractionHand hand, ItemStack stack,
                                                   TardisConsoleBlockEntity console, Level level) {
        UUID tardisId = console.getTardisId();
        if (stack.get(GCDataComponents.TARDIS_ID) != null || tardisId == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack linked = stack.copyWithCount(1);
        linked.set(GCDataComponents.TARDIS_ID, tardisId);
        linked.set(GCDataComponents.KEY_INSERTED, false);

        if (stack.getCount() == 1) {
            player.setItemInHand(hand, linked);
        } else {
            stack.shrink(1);
            if (!player.getInventory().add(linked)) {
                player.drop(linked, false, Prediction.SERVER_ONLY);
            }
        }

        player.sendOverlayMessage(Component.translatable("message.gallifreyan_chronicles.key_linked"));
        return InteractionResult.CONSUME;
    }

    public TardisKeyItem(Properties properties) {
        super(properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.KEY));
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();

        if (level.getBlockEntity(context.getClickedPos()) instanceof TardisConsoleBlockEntity console) {
            return linkToConsole(player, context.getHand(), stack, console, level);
        }

        if (!(level.getBlockEntity(context.getClickedPos()) instanceof TardisExteriorBlockEntity exterior)) {
            return InteractionResult.PASS;
        }

        UUID keyTardisId = stack.get(GCDataComponents.TARDIS_ID);
        UUID exteriorTardisId = exterior.getTardisId();

        if (keyTardisId == null || !keyTardisId.equals(exteriorTardisId)) {
            return InteractionResult.FAIL;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        boolean inserted = Boolean.TRUE.equals(stack.get(GCDataComponents.KEY_INSERTED));

        if (!inserted) {
            if (exterior.hasKeyRendered()) return InteractionResult.FAIL;

            if (exterior.rejectKeyIfLockBroken(player)) return InteractionResult.FAIL;

            if (!exterior.areDoorsFullyClosed()) {
                player.sendOverlayMessage(Component.translatable("message.gallifreyan_chronicles.close_doors_to_insert_key"));
                return InteractionResult.FAIL;
            }

            exterior.insertKey(player, context.getHand(), stack);
        } else {
            if (!TardisApi.of(exterior).map(tardis -> tardis.returnKeyTo(player)).orElse(false)) {
                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.CONSUME;
    }
}
