package com.noahtnt2009.gallifreyan_chronicles.mixin.server;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.fright.TardisFright;

import com.noahtnt2009.gallifreyan_chronicles.compat.voicechat.TardisVoiceLinks;
import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisKeyLocator;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisDimensionManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisDeletion;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import com.noahtnt2009.gallifreyan_chronicles.util.GCServerState;
import com.noahtnt2009.gallifreyan_chronicles.sound.TimedSounds;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "createLevels", at = @At("TAIL"))
    private void gallifreyan_chronicles$restoreInteriors(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        if (gallifreyan_chronicles$isReplay(server)) return;
        GCServerState.onServerStart();
        TardisPortalBridge.reset();
        TardisDimensionManager.restoreAll(server);
        TardisAlarm.resumeCrashAlarms(server);
        TimedSounds.onServerStart(server);
        TardisPortalBridge.onDimensionsChanged(server);
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void gallifreyan_chronicles$processEntries(BooleanSupplier haveTime, CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        if (gallifreyan_chronicles$isReplay(server)) return;
        TardisDeletion.tick(server);
        TardisDimensionManager.processPendingEntries(server);
        GCScheduler.tick(server);
        TardisKeyLocator.tick(server);
        TardisPortalBridge.tick(server);
        ConsoleSyncSystem.tick(server);
        TardisVoiceLinks.tick(server);
        TardisFright.tick(server);
    }

    @Unique
    private static boolean gallifreyan_chronicles$isReplay(MinecraftServer server) {
        return server.getClass().getName().startsWith("com.moulberry.flashback.");
    }
}
