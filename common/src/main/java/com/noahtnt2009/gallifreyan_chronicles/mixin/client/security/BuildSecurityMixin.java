package com.noahtnt2009.gallifreyan_chronicles.mixin.client.security;

import com.noahtnt2009.gallifreyan_chronicles.client.security.BuildExpiry;
import com.noahtnt2009.gallifreyan_chronicles.client.security.BuildLock;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class BuildSecurityMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void gc$enforceBuildSecurity(CallbackInfo ci) {
        BuildLock.enforce(((Minecraft) (Object) this).getUser().getProfileId());
        BuildExpiry.enforceAtStartup();
    }
}
