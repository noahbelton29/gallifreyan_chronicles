package com.noahtnt2009.gallifreyan_chronicles.mixin;

import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Unique
    private static final String[] GC_ICONS = {
            "icon256x256.png", "icon48x48.png", "icon32x32.png", "icon16x16.png"
    };

    @Shadow
    private void setIcon(List<IoSupplier<InputStream>> iconStreams) throws IOException {
    }

    @Unique
    private static final Logger GC_LOG = LogUtils.getLogger();

    @ModifyVariable(method = "createWindow", at = @At("HEAD"), argsOnly = true)
    private String gc$initialTitle(String title) {
        return gc$rename(title);
    }

    @ModifyVariable(method = "setTitle", at = @At("HEAD"), argsOnly = true)
    private String gc$newTitle(String title) {
        return gc$rename(title);
    }

    @Unique
    private static String gc$rename(String title) {
        return title.replaceFirst("^Minecraft", "Gallifreyan Chronicles");
    }

    @Inject(
            method = "setIcon(Lnet/minecraft/server/packs/PackMetadataResources;Lcom/mojang/blaze3d/platform/IconSet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gc$customIcon(PackMetadataResources resources, IconSet iconSet, CallbackInfo ci) throws IOException {
        GC_LOG.info("[GC] WindowMixin setIcon injected");
        ClassLoader loader = Window.class.getClassLoader();
        List<IoSupplier<InputStream>> icons = new ArrayList<>();
        for (String name : GC_ICONS) {
            String path = "assets/gallifreyan_chronicles/icons/" + name;
            if (loader.getResource(path) != null) {
                icons.add(() -> Objects.requireNonNull(loader.getResourceAsStream(path)));
            } else {
                GC_LOG.warn("[GC] icon not found: {}", path);
            }
        }
        if (icons.isEmpty()) return;
        this.setIcon(icons);
        ci.cancel();
    }
}