package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record TardisConsoleSyncPayload(List<TardisConsole> consoles, String defaultId) implements CustomPacketPayload {
    public static final Type<TardisConsoleSyncPayload> TYPE =
            new Type<>(GCUtils.of("tardis_console_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisConsoleSyncPayload> STREAM_CODEC = StreamCodec.composite(
            TardisConsole.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsoleSyncPayload::consoles,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleSyncPayload::defaultId,
            TardisConsoleSyncPayload::new
    );

    public static TardisConsoleSyncPayload create() {
        TardisConsole defaultConsole = TardisConsoleRegistry.getDefault();
        return new TardisConsoleSyncPayload(
                List.copyOf(TardisConsoleRegistry.getAll()),
                defaultConsole != null ? defaultConsole.id() : ""
        );
    }

    public void apply() {
        GCSyncPayload.apply(consoles, TardisConsoleRegistry::clear, TardisConsoleRegistry::register, "TARDIS Console");
        if (defaultId != null && TardisConsoleRegistry.contains(defaultId)) {
            TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(defaultId));
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
