package uk.nbforge.gallifreyan_chronicles.mixin.client.render;

import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.client.renderer.fade.FadeBoxRenderer;

/** A fading TARDIS is not drawn with everything else, it is kept for its own canvas later. */
@Mixin(PreparedRenderType.class)
public abstract class PreparedRenderTypeFadeMixin {
    @Inject(method = "drawFromBuffer", at = @At("HEAD"), cancellable = true)
    private void gc$holdFadingBox(StagedVertexBuffer.ExecuteInfo info, RenderPass renderPass, CallbackInfo ci) {
        if (FadeBoxRenderer.hold((PreparedRenderType) (Object) this, info)) ci.cancel();
    }
}
