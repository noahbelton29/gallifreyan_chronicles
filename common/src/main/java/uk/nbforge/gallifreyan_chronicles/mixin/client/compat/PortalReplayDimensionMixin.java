package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import qouteall.imm_ptl.core.network.ImmPtlNetworking;
import qouteall.imm_ptl.core.portal.Portal;

/**
 * tardis dimensions dont exist in a replay so the portal packet for one throws and disconnects u with a network
 * protocol error, skip just that packet. also in a replay the entity type in the portal packet can come out wrong (it
 * said TardisControlEntity) and immersive portals refused the portal. all tardis portals are the normal portal type so
 * fix the type
 */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.network.ImmPtlNetworkingClient", remap = false)
public abstract class PortalReplayDimensionMixin {

    @Unique
    private static ImmPtlNetworking.PortalSyncPacket gc$fixType(ImmPtlNetworking.PortalSyncPacket packet) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || !server.getClass().getName().startsWith("com.moulberry.flashback.")) return packet;
        var portalType = Portal.ENTITY_TYPE;
        var type = packet.entityType();
        if (type == portalType) return packet;
        var registry = BuiltInRegistries.ENTITY_TYPE;
        var key = type == null ? null : registry.getKey(type);
        var portalKey = registry.getKey(portalType);
        if (key != null && key.getNamespace().equals(portalKey.getNamespace())) return packet;
        return new ImmPtlNetworking.PortalSyncPacket(
                packet.id(),
                packet.uuid(),
                portalType,
                packet.dimensionId(),
                packet.x(),
                packet.y(),
                packet.z(),
                packet.extraData());
    }

    @WrapMethod(method = "handlePortalSync", remap = false, require = 0)
    private static void gc$ignoreMissingDimension(ImmPtlNetworking.PortalSyncPacket packet, Operation<Void> original) {
        packet = gc$fixType(packet);
        try {
            original.call(packet);
        } catch (RuntimeException e) {
            String message = String.valueOf(e.getMessage());
            if (!message.startsWith("Cannot create invalid client dimension")) throw e;
        }
    }
}
