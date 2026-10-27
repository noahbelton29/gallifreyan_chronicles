package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.item.ScrollSelectable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeTardisScrollMixin {
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void gallifreyan_chronicles$cycleTardis(double mouseX, double mouseY, double scrollX, double scrollY,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (scrollY == 0.0) return;

        Slot slot = ((AbstractContainerScreenAccessor) (Object) this).gallifreyan_chronicles$getHoveredSlot();
        if (slot == null || slot.container instanceof Inventory) return;

        ItemStack stack = slot.getItem();
        if (!(stack.getItem() instanceof ScrollSelectable selectable)) return;

        if (selectable.cycleSelection(stack, scrollY > 0.0 ? -1 : 1)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            cir.setReturnValue(true);
        }
    }
}
