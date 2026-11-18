package uk.nbforge.gallifreyan_chronicles.platform;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import uk.nbforge.gallifreyan_chronicles.platform.services.INetworkHelper;

public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public void send(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
