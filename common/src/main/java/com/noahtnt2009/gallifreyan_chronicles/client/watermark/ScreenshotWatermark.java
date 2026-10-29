package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

import com.mojang.blaze3d.platform.NativeImage;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ScreenshotWatermark {
    private static final int MIN_SIZE = 256;

    private static final Set<NativeImage> DONE = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private ScreenshotWatermark() {
    }

    public static void apply(NativeImage image) {
        if (DONE.contains(image)) return;
        try {
            Constants.LOG.info("[GC] Screenshot callback: {}x{}, watermark enabled={}",
                    image.getWidth(), image.getHeight(), WatermarkIdentity.enabled());
            if (!WatermarkIdentity.enabled()) return;
            if (image.getWidth() < MIN_SIZE || image.getHeight() < MIN_SIZE) return;

            User user = Minecraft.getInstance().getUser();
            String label = WatermarkIdentity.label(user.getName(), user.getProfileId());
            WatermarkRenderer.apply(new NativeImageSurface(image), label, WatermarkIdentity.seed(label));
            DONE.add(image);
            Constants.LOG.info("[GC] Watermarked screenshot with build ID {}", WatermarkIdentity.buildId());
        } catch (Throwable t) {
            Constants.LOG.error("Failed to watermark screenshot", t);
        }
    }

    private record NativeImageSurface(NativeImage image) implements PixelSurface {
        @Override
        public int width() {
            return image.getWidth();
        }

        @Override
        public int height() {
            return image.getHeight();
        }

        @Override
        public int getArgb(int x, int y) {
            return image.getPixel(x, y);
        }

        @Override
        public void setArgb(int x, int y, int argb) {
            image.setPixel(x, y, argb);
        }
    }
}
