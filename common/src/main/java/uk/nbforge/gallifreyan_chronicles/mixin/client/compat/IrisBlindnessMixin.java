package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.client.gui.hud.GCPowerDarkness;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
public abstract class IrisBlindnessMixin {
    @Inject(method = "getBlindness", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void gc$powerDarkness(CallbackInfoReturnable<Float> cir) {
        GCPowerDarkness.noteShaderBlindnessRead();
        float darkness = GCPowerDarkness.renderLevelDarkness();
        if (darkness > 0.0F) {
            cir.setReturnValue(Math.max(cir.getReturnValueF(), darkness * GCPowerDarkness.SHADER_BLINDNESS_SCALE));
        }
    }
}
