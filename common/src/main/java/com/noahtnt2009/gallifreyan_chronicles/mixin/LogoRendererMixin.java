package com.noahtnt2009.gallifreyan_chronicles.mixin;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoRenderer.class)
public class LogoRendererMixin {
    @Unique
    private static final Identifier TITLE_LOGO = GCUtils.of("textures/gui/title/logo.png");

    @Unique
    private static final int gallifreyan_chronicles_26_1_2$WIDTH = (int)(256 * 0.9f);
    @Unique
    private static final int gallifreyan_chronicles_26_1_2$HEIGHT = (int)(60 * 0.9f);

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void replaceLogoRender(GuiGraphicsExtractor graphics, int width, float alpha, CallbackInfo ci) {
        ci.cancel();
        LogoRenderer self = (LogoRenderer)(Object)this;
        int x = width / 2 - gallifreyan_chronicles_26_1_2$WIDTH / 2;
        float effectiveAlpha = self.keepLogoThroughFade() ? 1.0F : alpha;
        int color = ARGB.white(effectiveAlpha);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TITLE_LOGO, x, 18, 0.0F, 0.0F,  // 30 - 12 = 18
                gallifreyan_chronicles_26_1_2$WIDTH, gallifreyan_chronicles_26_1_2$HEIGHT,
                gallifreyan_chronicles_26_1_2$WIDTH, gallifreyan_chronicles_26_1_2$HEIGHT, color);
    }
}