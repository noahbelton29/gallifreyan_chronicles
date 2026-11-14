package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.client.renderer.util.OutlineHider;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.client.renderer.extract.LevelExtractor")
public abstract class LevelExtractorOutlineMixin {
    @Inject(method = "extractBlockOutline", at = @At("HEAD"), cancellable = true, require = 0)
    private void gc$hideTardisDoorOutline(Camera camera, LevelRenderState state, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit && hit.getType() != HitResult.Type.MISS
                && OutlineHider.shouldHide(mc.level, blockHit.getBlockPos())) {
            ci.cancel();
        }
    }
}
