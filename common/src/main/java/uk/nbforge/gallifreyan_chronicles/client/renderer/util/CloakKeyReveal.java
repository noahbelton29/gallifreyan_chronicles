package uk.nbforge.gallifreyan_chronicles.client.renderer.util;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;

public final class CloakKeyReveal {
    private CloakKeyReveal() {}

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
