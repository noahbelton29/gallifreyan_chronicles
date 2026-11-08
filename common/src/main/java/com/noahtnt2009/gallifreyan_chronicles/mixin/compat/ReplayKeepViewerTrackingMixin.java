package com.noahtnt2009.gallifreyan_chronicles.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.network.PacketRedirection;
import net.minecraft.network.protocol.Packet;

/* immersive portals does its own "who can see this entity" check. the flashback
   replay camera never looks loaded to it, so once a portal shows up it stopped
   sending entities (like the recorded player) to the camera and they froze.
   we never take the replay camera off an entity's watcher list.
   ip_updateEntityTrackingStatus is added by immersive portals, so priority is
   above theirs (1000) to run after it */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity", priority = 1500)
public abstract class ReplayKeepViewerTrackingMixin {
    @Shadow
    @Final
    private Entity entity;
    @Shadow
    @Final
    private ServerEntity serverEntity;
    @Shadow
    @Final
    private Set<ServerPlayerConnection> seenBy;

    /* the other half: immersive portals never adds the replay camera either.
       after jumping back on the timeline every entity is made again, so nothing
       was sent to the camera and the player froze. add the camera ourselves */
    @Inject(method = "ip_updateEntityTrackingStatus", at = @At("TAIL"), remap = false, require = 0)
    private void gc$addReplayCamera(CallbackInfo ci) {
        this.gc$ensureCamera();
    }

    /* flashback sends recorded movement through here, also while paused or
       jumping on the timeline (when immersive portals doesn't run). so make sure
       the camera is on the list first, or the moves go to nobody */
    @Inject(method = "sendToTrackingPlayers", at = @At("HEAD"), require = 0)
    private void gc$addCameraBeforeSend(Packet<?> packet, CallbackInfo ci) {
        this.gc$ensureCamera();
    }

    private void gc$ensureCamera() {
        if (!(this.entity.level() instanceof ServerLevel level)) return;
        if (!level.getServer().getClass().getName().startsWith("com.moulberry.flashback.")) return;
        for (ServerPlayer player : level.players()) {
            if (player == this.entity || !gc$isReplayCamera(player.connection)) continue;
            if (this.seenBy.add(player.connection)) {
                PacketRedirection.withForceRedirect(level, () -> this.serverEntity.addPairing(player));
            }
        }
    }

    @WrapOperation(method = "ip_updateEntityTrackingStatus",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;removeIf(Ljava/util/function/Predicate;)Z"),
            remap = false, require = 0)
    private boolean gc$keepReplayCamera(Set<ServerPlayerConnection> seenBy, Predicate<ServerPlayerConnection> check,
                                        Operation<Boolean> original) {
        Predicate<ServerPlayerConnection> keepCamera = c -> !gc$isReplayCamera(c) && check.test(c);
        return original.call(seenBy, keepCamera);
    }

    private static boolean gc$isReplayCamera(ServerPlayerConnection c) {
        return c.getPlayer().getClass().getName().equals("com.moulberry.flashback.playback.ReplayPlayer");
    }
}
