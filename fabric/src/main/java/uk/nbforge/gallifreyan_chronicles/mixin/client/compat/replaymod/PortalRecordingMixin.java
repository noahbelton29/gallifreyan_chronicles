package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.replaymod;

import com.replaymod.recording.packet.PacketListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.network.PacketRedirection;
import qouteall.q_misc_util.dimension.DimIntIdMap;
import qouteall.q_misc_util.dimension.DimensionIntId;

@Pseudo
@Mixin(targets = "com.replaymod.recording.handler.RecordingEventHandler", remap = false)
public abstract class PortalRecordingMixin {

    @Shadow
    @Final
    private Minecraft mc;
    @Shadow
    @Final
    private PacketListener packetListener;

    @Shadow
    public abstract void spawnRecordingPlayer();

    @Unique
    private ResourceKey<Level> gc$baseDim;
    @Unique
    private ResourceKey<Level> gc$lastDim;
    @Unique
    private boolean gc$switching;

    @Inject(method = "spawnRecordingPlayer", at = @At("HEAD"), remap = false, require = 0)
    private void gc$trackBase(CallbackInfo ci) {
        if (gc$switching) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        gc$baseDim = player.level().dimension();
        gc$lastDim = gc$baseDim;
    }

    @Inject(method = "onPlayerTick", at = @At("HEAD"), remap = false, require = 0)
    private void gc$followPortalCrossing(CallbackInfo ci) {
        LocalPlayer player = mc.player;
        if (player == null || gc$baseDim == null || gc$lastDim == null) return;
        ResourceKey<Level> current = player.level().dimension();
        if (current.equals(gc$lastDim)) return;

        gc$saveIn(gc$lastDim, new ClientboundRemoveEntitiesPacket(player.getId()));
        gc$lastDim = current;
        gc$switching = true;
        try {
            spawnRecordingPlayer();
        } finally {
            gc$switching = false;
        }
    }

    @Redirect(
            method = {"onPacket", "spawnRecordingPlayer", "onClientEffect", "onPlayerTick", "onBlockBreakAnim"},
            at = @At(value = "INVOKE",
                    target = "Lcom/replaymod/recording/packet/PacketListener;save(Lnet/minecraft/network/protocol/Packet;)V"),
            remap = false, require = 0)
    private void gc$saveInPlayerLevel(PacketListener listener, Packet packet) {
        ResourceKey<Level> dim = gc$lastDim;
        if (dim == null) {
            listener.save(packet);
            return;
        }
        gc$saveIn(dim, packet);
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void gc$saveIn(ResourceKey<Level> dim, Packet packet) {
        if (gc$baseDim == null || dim.equals(gc$baseDim)) {
            packetListener.save(packet);
            return;
        }
        DimIntIdMap map = DimensionIntId.getClientMap();
        int id = map.toIntegerId(dim);
        if (id == DimIntIdMap.MISSING_ID) {
            packetListener.save(packet);
            return;
        }
        packetListener.save(new ClientboundCustomPayloadPacket(new PacketRedirection.Payload(id, packet)));
    }
}
