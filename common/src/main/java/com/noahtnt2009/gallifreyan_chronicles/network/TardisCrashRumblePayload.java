package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.camera.GCCameraShake;
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

/* a harsh rumble for the whole crash */
public record TardisCrashRumblePayload(double x, double y, double z, int ticks) implements CustomPacketPayload {
    public static final Type<TardisCrashRumblePayload> TYPE =
            new Type<>(GCUtils.modIdentifier("tardis_crash_rumble"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisCrashRumblePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            TardisCrashRumblePayload::x,
            ByteBufCodecs.DOUBLE,
            TardisCrashRumblePayload::y,
            ByteBufCodecs.DOUBLE,
            TardisCrashRumblePayload::z,
            ByteBufCodecs.VAR_INT,
            TardisCrashRumblePayload::ticks,
            TardisCrashRumblePayload::new
    );

    public static void broadcast(ServerLevel level, Vec3 origin, int ticks) {
        TardisCrashRumblePayload payload = new TardisCrashRumblePayload(origin.x, origin.y, origin.z, ticks);
        for (ServerPlayer player : level.players()) {
            Services.NETWORK.sendTardisCrashRumble(player, payload);
        }
    }

    public void apply() {
        GCCameraShake.crashRumble(new Vec3(x, y, z), ticks);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
