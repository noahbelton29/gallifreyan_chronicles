package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import uk.nbforge.gallifreyan_chronicles.network.GCSyncPayload;

public record TardisConsoleSyncPayload(List<TardisConsole> consoles, List<TardisConsoleVariant> variants,
                                       String defaultId,
                                       List<TardisDoorVariant> doorVariants) implements CustomPacketPayload {
    public static final Type<TardisConsoleSyncPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_console_sync"));

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
        Set<String> previousIds = GCSyncPayload.idsOf(TardisConsoleRegistry.getAll());

        GCSyncPayload.apply(consoles, TardisConsoleRegistry::clear, TardisConsoleRegistry::register, "TARDIS Console");
        GCSyncPayload.apply(variants, TardisConsoleRegistry::clearVariants, TardisConsoleRegistry::registerVariant, "TARDIS Console Variant");
        GCSyncPayload.apply(doorVariants, TardisDoorRegistry::clear, TardisDoorRegistry::register, "TARDIS Door Variant");
        if (defaultId != null && TardisConsoleRegistry.contains(defaultId)) {
            TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(defaultId));
        }

        GCSyncPayload.reloadIfChanged(previousIds, consoles);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
