package com.noahtnt2009.gallifreyan_chronicles.tardis.security;

import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public final class TardisTrust {
    private TardisTrust() {
    }

    public static InteractionResult interact(Player clicker, Level level, InteractionHand hand, Entity entity) {
        if (hand != InteractionHand.MAIN_HAND || level.isClientSide()) return InteractionResult.PASS;
        if (!(clicker instanceof ServerPlayer owner) || !(entity instanceof ServerPlayer target) || owner == target) {
            return InteractionResult.PASS;
        }

        MinecraftServer server = level.getServer();
        if (server == null) return InteractionResult.PASS;

        Optional<Tardis> held = heldKeyTardis(server, owner);
        if (held.isEmpty()) return InteractionResult.PASS;

        Tardis tardis = held.get();
        if (!tardis.isOwner(owner)) {
            Messages.overlay(owner, "trust_not_yours");
            return InteractionResult.SUCCESS;
        }

        return owner.isShiftKeyDown() ? untrust(tardis, owner, target) : trust(tardis, owner, target);
    }

    private static InteractionResult trust(Tardis tardis, ServerPlayer owner, ServerPlayer target) {
        if (!tardis.trust(target.getUUID())) {
            Messages.overlay(owner, "trust_already", target.getDisplayName());
            return InteractionResult.SUCCESS;
        }

        Messages.overlay(owner, "trust_added", target.getDisplayName());
        Messages.overlay(target, "trust_granted", owner.getDisplayName());
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult untrust(Tardis tardis, ServerPlayer owner, ServerPlayer target) {
        if (!tardis.untrust(target.getUUID())) {
            Messages.overlay(owner, "trust_not_trusted", target.getDisplayName());
            return InteractionResult.SUCCESS;
        }

        Messages.overlay(owner, "trust_removed", target.getDisplayName());
        Messages.overlay(target, "trust_revoked", owner.getDisplayName());
        return InteractionResult.SUCCESS;
    }

    private static Optional<Tardis> heldKeyTardis(MinecraftServer server, Player holder) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = holder.getItemInHand(hand);
            if (stack.getItem() != GCItems.TARDIS_KEY) continue;
            UUID tardisId = stack.get(GCDataComponents.TARDIS_ID);
            Optional<Tardis> tardis = TardisApi.get(server, tardisId);
            if (tardis.isPresent()) return tardis;
        }
        return Optional.empty();
    }
}
