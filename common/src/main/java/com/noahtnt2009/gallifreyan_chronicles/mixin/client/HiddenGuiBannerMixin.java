package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.client.watermark.HudVisibility;
import com.noahtnt2009.gallifreyan_chronicles.client.watermark.WatermarkBanner;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.client.gui.Gui", "net.minecraft.client.gui.Hud"})
public abstract class HiddenGuiBannerMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("RETURN"),
            require = 0
    )
    private void gallifreyan_chronicles$drawBannerWhenHidden(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (HudVisibility.isHidden()) {
            WatermarkBanner.render(graphics);
        }
    }
}
