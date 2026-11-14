package uk.nbforge.gallifreyan_chronicles.mixin.server;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.fright.TardisFright;

import uk.nbforge.gallifreyan_chronicles.compat.voicechat.TardisVoiceLinks;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.tardis.key.TardisKeyLocator;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionManager;
import uk.nbforge.gallifreyan_chronicles.tardis.manager.deletion.TardisDeletion;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerLifecycleHandler;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
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
        ServerLifecycleHandler.onServerStart();
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
        GCSchedulerUtil.tick(server);
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
