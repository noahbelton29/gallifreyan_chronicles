package uk.nbforge.gallifreyan_chronicles.mixin.client.render;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.client.renderer.fade.FadeBoxRenderer;
import uk.nbforge.gallifreyan_chronicles.client.sky.CustomSkyRenderer;

/** Draws fading TARDISes on top once the world, water and clouds are all done. */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererFadeBoxMixin {
    @Shadow
    @Final
    private GameRenderer gameRenderer;

    @Inject(method = "render", at = @At("HEAD"))
    private void gc$forgetOldBoxes(CallbackInfo ci) {
        FadeBoxRenderer.clear();
    }

    @Inject(method = "blitEntityOutline", at = @At("HEAD"))
    private void gc$layCanvasAfterShaders(CallbackInfo ci) {
        CustomSkyRenderer.afterShaders(this.gameRenderer.mainRenderTarget());
        FadeBoxRenderer.afterShaders(this.gameRenderer.mainRenderTarget());
    }

    @Inject(method = "executeOutline", at = @At("HEAD"))
    private void gc$drawFadingBoxes(FeatureRenderDispatcher.PreparedFrame featureFrame, CallbackInfo ci) {
        CustomSkyRenderer.drawCanvas(this.gameRenderer.mainRenderTarget());
        FadeBoxRenderer.drawAndComposite(this.gameRenderer.mainRenderTarget());
    }
}
