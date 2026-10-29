package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.watermark.ScreenshotWatermark;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(Screenshot.class)
public abstract class ScreenshotMixin {
    @ModifyVariable(method = "takeScreenshot*", at = @At("HEAD"), argsOnly = true, name = "callback")
    private static Consumer<NativeImage> gallifreyan_chronicles$watermark(Consumer<NativeImage> original) {
        Constants.LOG.info("[GC] Screenshot hook reached");
        return image -> {
            ScreenshotWatermark.apply(image);
            original.accept(image);
        };
    }
}
