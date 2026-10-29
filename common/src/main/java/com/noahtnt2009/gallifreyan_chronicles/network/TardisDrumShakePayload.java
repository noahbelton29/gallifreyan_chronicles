package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.GCClientHooks;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public record TardisDrumShakePayload(double x, double y, double z, float strength) implements CustomPacketPayload {
    private static final double MAX_DISTANCE = 25.0;

    public static final Type<TardisDrumShakePayload> TYPE =
            new Type<>(GCUtils.modIdentifier("tardis_drum_shake"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisDrumShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            TardisDrumShakePayload::x,
            ByteBufCodecs.DOUBLE,
            TardisDrumShakePayload::y,
            ByteBufCodecs.DOUBLE,
            TardisDrumShakePayload::z,
            ByteBufCodecs.FLOAT,
            TardisDrumShakePayload::strength,
            TardisDrumShakePayload::new
    );

    public static void broadcast(ServerLevel level, Vec3 origin, float strength) {
        TardisDrumShakePayload payload = new TardisDrumShakePayload(origin.x, origin.y, origin.z, strength);
        double maxDistanceSqr = MAX_DISTANCE * MAX_DISTANCE;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(origin) < maxDistanceSqr) {
                Services.NETWORK.sendTardisDrumShake(player, payload);
            }
        }
    }

    public void apply() {
        GCClientHooks.triggerCameraShake(new Vec3(x, y, z), strength);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
