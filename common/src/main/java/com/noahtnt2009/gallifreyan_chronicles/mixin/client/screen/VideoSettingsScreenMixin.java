package com.noahtnt2009.gallifreyan_chronicles.mixin.client.screen;

import com.noahtnt2009.gallifreyan_chronicles.client.gui.options.GCOptions;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {
    private VideoSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void gc$addSkyOptions(CallbackInfo ci) {
        if (this.list != null) {
            this.list.addHeader(GCOptions.HEADER);
            this.list.addBig(GCOptions.starDensity());
            this.list.addBig(GCOptions.modelRenderDistance());
            this.list.addBig(GCOptions.immersivePortals());
            this.list.addBig(GCOptions.portalRenderDistance());
            this.list.addBig(GCOptions.maxRenderedPortals());
        }
    }
}
