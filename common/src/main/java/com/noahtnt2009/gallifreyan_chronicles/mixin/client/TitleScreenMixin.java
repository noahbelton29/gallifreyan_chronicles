package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.client.title.GCTitleScreenLayout;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void gc$buildTitleScreen(CallbackInfo ci) {
        if (this.minecraft.isDemo()) return;
        GCTitleScreenLayout.build(this, this.minecraft, this.font, this.width, this.height, this::gc$addWidget);
        ci.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void gc$renderTitleScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.minecraft.isDemo()) return;
        GCTitleScreenLayout.renderBackdrop(graphics, this.width, this.height);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        GCTitleScreenLayout.renderOverlay(graphics, this.font, this.width, this.height);
        ci.cancel();
    }

    @Unique
    private void gc$addWidget(AbstractWidget widget) {
        this.addRenderableWidget(widget);
    }
}
