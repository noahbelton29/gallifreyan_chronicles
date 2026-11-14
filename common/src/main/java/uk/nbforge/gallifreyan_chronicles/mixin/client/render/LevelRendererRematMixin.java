package uk.nbforge.gallifreyan_chronicles.mixin.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* draws the fading interior for a player a tardis materialised around */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererRematMixin {
    @Inject(method = "submitTransientBlocks", at = @At("HEAD"), require = 0)
    private void gc$submitRematGhost(PoseStack poseStack, SubmitNodeCollector collector, LevelRenderState state,
                                     CallbackInfo ci) {
        RematCatchClient.submit(poseStack, collector, state.cameraRenderState.pos, Minecraft.getInstance().level);
    }
}
