package com.noahtnt2009.gallifreyan_chronicles.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Portal;

/* flashback replays have portals with no data (axisW is null).
   a portal like that crashed the join, then the renderer.
   so we never send them to the viewer. */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.portal.Portal", remap = false)
public abstract class PortalReplayServerMixin {

    /* normal way: don't track an empty portal at all */
    @Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$skipEmptyPortal(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (((Portal) (Object) this).getAxisW() == null) {
            cir.setReturnValue(false);
        }
    }

    /* backup: if it still gets here, send a harmless "remove" packet, not a broken portal */
    @WrapMethod(method = "getAddEntityPacket", remap = false, require = 0)
    private Packet<ClientGamePacketListener> gc$safePacket(ServerEntity serverEntity,
                                                           Operation<Packet<ClientGamePacketListener>> original) {
        if (((Portal) (Object) this).getAxisW() != null) {
            return original.call(serverEntity);
        }
        return new ClientboundRemoveEntitiesPacket(((Entity) (Object) this).getId());
    }
}
