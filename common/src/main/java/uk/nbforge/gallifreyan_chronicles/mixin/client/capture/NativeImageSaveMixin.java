package uk.nbforge.gallifreyan_chronicles.mixin.client.capture;

import com.mojang.blaze3d.platform.NativeImage;
import java.nio.file.Path;
import java.util.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.client.protection.watermark.ScreenshotWatermark;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

@Mixin(NativeImage.class)
public abstract class NativeImageSaveMixin {
    @Inject(method = "writeToFile(Ljava/nio/file/Path;)V", at = @At("HEAD"), require = 0)
    private void gallifreyan_chronicles$watermarkOnSave(Path path, CallbackInfo ci) {
        String normalized = path.toString().replace('\\', '/').toLowerCase(Locale.ROOT);
        if (!normalized.contains("/screenshots/")) return;
        GCLog.TESTER.debug("NativeImage save hook reached: {}", path.getFileName());
        ScreenshotWatermark.apply((NativeImage) (Object) this);
    }
}
