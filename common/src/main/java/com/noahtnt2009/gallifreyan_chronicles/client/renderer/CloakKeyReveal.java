package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class CloakKeyReveal {
    private CloakKeyReveal() {
    }

    public static boolean isHoldingKeyFor(UUID tardisId) {
        if (tardisId == null) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == GCItems.MASTER_TARDIS_KEY) return true;
            if (stack.getItem() == GCItems.TARDIS_KEY && tardisId.equals(stack.get(GCDataComponents.TARDIS_ID))) {
                return true;
            }
        }
        return false;
    }
}
