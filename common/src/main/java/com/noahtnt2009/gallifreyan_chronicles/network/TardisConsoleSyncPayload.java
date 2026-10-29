package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorVariant;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.client.GCClientHooks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TardisConsoleSyncPayload(List<TardisConsole> consoles, List<TardisConsoleVariant> variants, String defaultId,
                                       List<TardisDoorVariant> doorVariants) implements CustomPacketPayload {
    public static final Type<TardisConsoleSyncPayload> TYPE =
            new Type<>(GCUtils.modIdentifier("tardis_console_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisConsoleSyncPayload> STREAM_CODEC = StreamCodec.composite(
            TardisConsole.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsoleSyncPayload::consoles,
            TardisConsoleVariant.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsoleSyncPayload::variants,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleSyncPayload::defaultId,
            TardisDoorVariant.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisConsoleSyncPayload::doorVariants,
            TardisConsoleSyncPayload::new
    );

    public static TardisConsoleSyncPayload create() {
        TardisConsole defaultConsole = TardisConsoleRegistry.getDefault();
        return new TardisConsoleSyncPayload(
                List.copyOf(TardisConsoleRegistry.getAll()),
                List.copyOf(TardisConsoleRegistry.getAllVariants()),
                defaultConsole != null ? defaultConsole.id() : "",
                List.copyOf(TardisDoorRegistry.getAll())
        );
    }

    public void apply() {
        Set<String> previousIds = TardisConsoleRegistry.getAll().stream()
                .map(TardisConsole::id)
                .collect(Collectors.toSet());

        GCSyncPayload.apply(consoles, TardisConsoleRegistry::clear, TardisConsoleRegistry::register, "TARDIS Console");
        GCSyncPayload.apply(variants, TardisConsoleRegistry::clearVariants, TardisConsoleRegistry::registerVariant, "TARDIS Console Variant");
        GCSyncPayload.apply(doorVariants, TardisDoorRegistry::clear, TardisDoorRegistry::register, "TARDIS Door Variant");
        if (defaultId != null && TardisConsoleRegistry.contains(defaultId)) {
            TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(defaultId));
        }

        Set<String> newIds = consoles.stream()
                .map(TardisConsole::id)
                .collect(Collectors.toSet());

        if (!newIds.equals(previousIds)) {
            GCClientHooks.reloadResourcePacks();
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
