package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;
import qouteall.imm_ptl.core.portal.Portal;

@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient", remap = false)
public abstract class PortalTardisClickMixin {
    @Unique
    private static HitResult gc$localHit;

    @Inject(method = "updatePointedBlock", at = @At("HEAD"), remap = false, require = 0)
    private static void gc$rememberLocalHit(float tickDelta, CallbackInfo ci) {
        gc$localHit = Minecraft.getInstance().hitResult;
    }

    @Inject(method = "updatePointedBlock", at = @At("RETURN"), remap = false, require = 0)
    private static void gc$fallBackToTardis(float tickDelta, CallbackInfo ci) {
        Portal portal = BlockManipulationClient.remotePointedPortal;
        HitResult local = gc$localHit;
        gc$localHit = null;
        if (portal == null) return;
        if (!TardisPortalBridge.PORTAL_TAG.equals(portal.portalTag)) return;
        if (TardisPortalBridge.isDoorBackBlocked(portal)) return;

        BlockManipulationClient.remotePointedDim = null;
        BlockManipulationClient.remoteHitResult = null;
        BlockManipulationClient.remotePointedPortal = null;

        Minecraft mc = Minecraft.getInstance();
        BlockPos base = TardisPortalBridge.exteriorBaseOf(portal);
        if (base == null) {
            if (local != null) mc.hitResult = local;
            return;
        }
        if (local instanceof BlockHitResult lh && lh.getType() != HitResult.Type.MISS
                && (lh.getBlockPos().equals(base) || lh.getBlockPos().equals(base.above()))) {
            mc.hitResult = local;
            return;
        }
        Vec3 at = portal.getOriginPos();
        mc.hitResult = new BlockHitResult(at, mc.player != null ? mc.player.getDirection().getOpposite() : Direction.NORTH,
                base, false);
    }
}
