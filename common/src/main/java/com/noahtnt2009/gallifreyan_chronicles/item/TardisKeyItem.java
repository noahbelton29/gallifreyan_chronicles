package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;

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
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class TardisKeyItem extends Item implements ScrollSelectable {
    public static final List<String> STYLES = List.of("tardis", "gallifreyan", "yale");
    private static final int TRUSTED_REFRESH_TICKS = 40;
    private static final int TRUSTED_SHOWN = 5;

    public static String styleOf(@Nullable String stored) {
        return stored != null && STYLES.contains(stored) ? stored : STYLES.getFirst();
    }

    @Override
    public boolean cycleSelection(ItemStack stack, int step) {
        int index = STYLES.indexOf(styleOf(stack.get(GCDataComponents.KEY_STYLE)));
        String next = STYLES.get(Math.floorMod(index + step, STYLES.size()));
        if (next.equals(STYLES.getFirst())) {
            stack.remove(GCDataComponents.KEY_STYLE);
        } else {
            stack.set(GCDataComponents.KEY_STYLE, next);
        }
        return true;
    }

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

        Messages.overlay(player, "key_linked");
        return InteractionResult.CONSUME;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if (level.getGameTime() % TRUSTED_REFRESH_TICKS != 0) return;
        UUID tardisId = stack.get(GCDataComponents.TARDIS_ID);
        if (tardisId == null) return;
        MinecraftServer server = level.getServer();
        List<String> names = new ArrayList<>();
        for (UUID player : TardisManager.get(server).trustedOf(tardisId)) {
            names.add(server.services().nameToIdCache().get(player)
                    .map(NameAndId::name)
                    .orElse(player.toString().substring(0, 8)));
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        if (names.isEmpty()) {
            if (stack.has(GCDataComponents.KEY_TRUSTED)) stack.remove(GCDataComponents.KEY_TRUSTED);
        } else if (!names.equals(stack.get(GCDataComponents.KEY_TRUSTED))) {
            stack.set(GCDataComponents.KEY_TRUSTED, List.copyOf(names));
        }
    }

    public static String trustedLine(List<String> names) {
        if (names.size() <= TRUSTED_SHOWN) return String.join(", ", names);
        return String.join(", ", names.subList(0, TRUSTED_SHOWN)) + " +" + (names.size() - TRUSTED_SHOWN);
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

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (keyTardisId == null || !keyTardisId.equals(exteriorTardisId)) {
            if (Boolean.TRUE.equals(stack.get(GCDataComponents.KEY_INSERTED)) || exterior.hasKeyRendered()) {
                return InteractionResult.FAIL;
            }
            if (exterior.isWrongKeyLockedOut(player, stack)) return InteractionResult.FAIL;
            if (!exterior.areDoorsFullyClosed()) {
                Messages.overlay(player, "close_doors_to_insert_key");
                return InteractionResult.FAIL;
            }
            exterior.insertWrongKey(player, context.getHand(), stack);
            return InteractionResult.CONSUME;
        }

        boolean inserted = Boolean.TRUE.equals(stack.get(GCDataComponents.KEY_INSERTED));

        if (!inserted) {
            if (exterior.hasKeyRendered()) return InteractionResult.FAIL;

            if (exterior.rejectKeyIfLockBroken(player)) return InteractionResult.FAIL;

            if (!exterior.areDoorsFullyClosed()) {
                Messages.overlay(player, "close_doors_to_insert_key");
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
