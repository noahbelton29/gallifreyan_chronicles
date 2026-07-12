package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        Set<String> previousIds = TardisConsoleRegistry.getAll().stream()
                .map(TardisConsole::id)
                .collect(Collectors.toSet());

        GCSyncPayload.apply(consoles, TardisConsoleRegistry::clear, TardisConsoleRegistry::register, "TARDIS Console");
        if (defaultId != null && TardisConsoleRegistry.contains(defaultId)) {
            TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(defaultId));
        }

        Set<String> newIds = consoles.stream()
                .map(TardisConsole::id)
                .collect(Collectors.toSet());

        if (!newIds.equals(previousIds)) {
            Minecraft mc = Minecraft.getInstance();
            mc.execute(mc::reloadResourcePacks);
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
