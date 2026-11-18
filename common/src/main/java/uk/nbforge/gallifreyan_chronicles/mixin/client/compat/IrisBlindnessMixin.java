package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.PowerDarkness;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
public abstract class IrisBlindnessMixin {
    @Inject(method = "getBlindness", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void gc$powerDarkness(CallbackInfoReturnable<Float> cir) {
        PowerDarkness.noteShaderBlindnessRead();
        float darkness = PowerDarkness.renderLevelDarkness();
        if (darkness > 0.0f) {
            cir.setReturnValue(Math.max(cir.getReturnValueF(), darkness * PowerDarkness.SHADER_BLINDNESS_SCALE));
        }
    }
}
