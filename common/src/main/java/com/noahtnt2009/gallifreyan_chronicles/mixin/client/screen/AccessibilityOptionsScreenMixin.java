package com.noahtnt2009.gallifreyan_chronicles.mixin.client.screen;

import com.noahtnt2009.gallifreyan_chronicles.client.gui.options.GCOptions;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsScreenMixin extends OptionsSubScreen {
    private AccessibilityOptionsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void gc$addTardisAccessibility(CallbackInfo ci) {
        if (this.list != null) {
            this.list.addSmall(GCOptions.cameraShake(), GCOptions.powerDarkness());
        }
    }
}
