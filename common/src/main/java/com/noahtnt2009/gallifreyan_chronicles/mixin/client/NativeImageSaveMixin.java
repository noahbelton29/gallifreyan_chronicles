package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.watermark.ScreenshotWatermark;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Locale;

@Mixin(NativeImage.class)
public abstract class NativeImageSaveMixin {
    @Inject(method = "writeToFile(Ljava/nio/file/Path;)V", at = @At("HEAD"), require = 0)
    private void gallifreyan_chronicles$watermarkOnSave(Path path, CallbackInfo ci) {
        String normalized = path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
        if (!normalized.contains("/screenshots/")) return;
        Constants.LOG.info("[GC] NativeImage save hook reached: {}", path.getFileName());
        ScreenshotWatermark.apply((NativeImage) (Object) this);
    }
}
