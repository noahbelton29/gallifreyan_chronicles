package uk.nbforge.gallifreyan_chronicles.mixin.client.startup;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.client.protection.build.TesterReport;
import uk.nbforge.gallifreyan_chronicles.client.renderer.detail.RenderCapability;
import uk.nbforge.gallifreyan_chronicles.client.renderer.detail.TrialTimer;

@Mixin(Minecraft.class)
public abstract class ClientInitMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void gc$onClientReady(CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        RenderCapability.apply(minecraft.getUser().getProfileId());
        TrialTimer.checkAtStartup();
        TesterReport.start(
                minecraft.getUser().getProfileId(), minecraft.getUser().getName());
    }
}
