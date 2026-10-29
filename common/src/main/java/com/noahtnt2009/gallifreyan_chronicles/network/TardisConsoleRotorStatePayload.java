package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.GCClientHooks;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record TardisConsoleRotorStatePayload(BlockPos pos, String rotorState) implements CustomPacketPayload {
    public static final Type<TardisConsoleRotorStatePayload> TYPE =
            new Type<>(GCUtils.modIdentifier("tardis_console_rotor_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisConsoleRotorStatePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TardisConsoleRotorStatePayload::pos,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleRotorStatePayload::rotorState,
            TardisConsoleRotorStatePayload::new
    );

    public void apply() {
        GCClientHooks.applyConsoleRotorState(pos, rotorState);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
