package uk.nbforge.gallifreyan_chronicles.mixin.client.security;

import uk.nbforge.gallifreyan_chronicles.client.protection.build.BuildExpiry;
import uk.nbforge.gallifreyan_chronicles.client.protection.build.BuildLock;
import uk.nbforge.gallifreyan_chronicles.client.protection.build.TesterReport;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class BuildSecurityMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void gc$enforceBuildSecurity(CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        BuildLock.enforce(minecraft.getUser().getProfileId());
        BuildExpiry.enforceAtStartup();
        TesterReport.start(minecraft.getUser().getProfileId(), minecraft.getUser().getName());
    }
}
