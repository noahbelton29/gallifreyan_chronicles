package uk.nbforge.gallifreyan_chronicles.tardis.security;

import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
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
            MessageUtil.overlay(owner, "trust_not_yours");
            return InteractionResult.SUCCESS;
        }

        return owner.isShiftKeyDown() ? untrust(tardis, owner, target) : trust(tardis, owner, target);
    }

    private static InteractionResult trust(Tardis tardis, ServerPlayer owner, ServerPlayer target) {
        if (!tardis.trust(target.getUUID())) {
            MessageUtil.overlay(owner, "trust_already", target.getDisplayName());
            return InteractionResult.SUCCESS;
        }

        MessageUtil.overlay(owner, "trust_added", target.getDisplayName());
        MessageUtil.overlay(target, "trust_granted", owner.getDisplayName());
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult untrust(Tardis tardis, ServerPlayer owner, ServerPlayer target) {
        if (!tardis.untrust(target.getUUID())) {
            MessageUtil.overlay(owner, "trust_not_trusted", target.getDisplayName());
            return InteractionResult.SUCCESS;
        }

        MessageUtil.overlay(owner, "trust_removed", target.getDisplayName());
        MessageUtil.overlay(target, "trust_revoked", owner.getDisplayName());
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
