package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/* keyframe camera positions go through ReplayKeyframePortal, so a camera path can
   fly in and out of doors */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.keyframe.handler.MinecraftKeyframeHandler", remap = false)
public abstract class ReplayKeyframeApplyMixin {
    @WrapOperation(method = "applyCameraPosition",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;snapTo(DDDFF)V"),
            remap = false, require = 0)
    private void gc$throughDoors(LocalPlayer player, double x, double y, double z, float yaw, float pitch,
                                 Operation<Void> original) {
        double[] p = FlashbackChunks.keyframeCamera(x, y, z, yaw, pitch);
        original.call(player, p[0], p[1], p[2], (float) p[3], (float) p[4]);
    }
}
