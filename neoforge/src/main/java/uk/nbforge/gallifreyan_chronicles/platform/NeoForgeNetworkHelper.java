package uk.nbforge.gallifreyan_chronicles.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import uk.nbforge.gallifreyan_chronicles.platform.services.INetworkHelper;

public class NeoForgeNetworkHelper implements INetworkHelper {
    @Override
    public void send(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
