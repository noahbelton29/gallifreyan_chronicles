package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.tardis.RematCatchClient;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/* a tardis materialised around a player and pulled them in.
   self: that player, who sees the interior fade in around them (box, floorY).
   everyone else in the interior: that player fades in like the tardis does */
public record RematCatchPayload(UUID player, String fadeType, int elapsedTicks, int durationTicks,
                                boolean self, BlockPos boxMin, BlockPos boxMax, int floorY)
        implements CustomPacketPayload {

    public static final Type<RematCatchPayload> TYPE = new Type<>(GCUtils.modIdentifier("remat_catch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RematCatchPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, p.player());
                ByteBufCodecs.STRING_UTF8.encode(buf, p.fadeType());
                buf.writeVarInt(p.elapsedTicks());
                buf.writeVarInt(p.durationTicks());
                buf.writeBoolean(p.self());
                buf.writeBlockPos(p.boxMin());
                buf.writeBlockPos(p.boxMax());
                buf.writeVarInt(p.floorY());
            },
            buf -> new RematCatchPayload(UUIDUtil.STREAM_CODEC.decode(buf), ByteBufCodecs.STRING_UTF8.decode(buf),
                    buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBlockPos(), buf.readBlockPos(),
                    buf.readVarInt())
    );

    public void apply() {
        RematCatchClient.start(this);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
