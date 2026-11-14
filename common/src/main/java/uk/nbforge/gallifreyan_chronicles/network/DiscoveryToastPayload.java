package uk.nbforge.gallifreyan_chronicles.network;

import uk.nbforge.gallifreyan_chronicles.client.gui.toast.DiscoveryToast;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

/* kind is personality, trait or disability */
public record DiscoveryToastPayload(String kind, String id) implements CustomPacketPayload {
    public static final Type<DiscoveryToastPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("discovery_toast"));

    public static void send(ServerPlayer player, String kind, String id) {
        Services.NETWORK.sendDiscoveryToast(player, new DiscoveryToastPayload(kind, id));
    }

    public void apply() {
        DiscoveryToast.show(kind, id);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
