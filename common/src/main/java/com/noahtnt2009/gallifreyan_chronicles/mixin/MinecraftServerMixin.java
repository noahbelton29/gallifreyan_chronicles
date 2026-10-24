package com.noahtnt2009.gallifreyan_chronicles.mixin;

import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "createLevels", at = @At("TAIL"))
    private void gallifreyan_chronicles$restoreInteriors(CallbackInfo ci) {
        GCScheduler.clear();
        TardisDimensionManager.restoreAll((MinecraftServer) (Object) this);
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void gallifreyan_chronicles$processEntries(BooleanSupplier haveTime, CallbackInfo ci) {
        TardisDimensionManager.processPendingEntries((MinecraftServer) (Object) this);
        TardisInteriorSystem.tickExitZones((MinecraftServer) (Object) this);
        GCScheduler.tick((MinecraftServer) (Object) this);
    }
}
