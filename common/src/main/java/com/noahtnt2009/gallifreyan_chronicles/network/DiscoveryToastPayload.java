package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.gui.toast.DiscoveryToast;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

/* kind is personality, trait or disability */
public record DiscoveryToastPayload(String kind, String id) implements CustomPacketPayload {
    public static final Type<DiscoveryToastPayload> TYPE =
            new Type<>(GCUtils.modIdentifier("discovery_toast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DiscoveryToastPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            DiscoveryToastPayload::kind,
            ByteBufCodecs.STRING_UTF8,
            DiscoveryToastPayload::id,
            DiscoveryToastPayload::new
    );

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
