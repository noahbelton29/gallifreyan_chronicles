package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import uk.nbforge.gallifreyan_chronicles.network.GCSyncPayload;

public record TardisExteriorSyncPayload(List<TardisExterior> exteriors,
                                        String defaultId) implements CustomPacketPayload {
    public static final Type<TardisExteriorSyncPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_exterior_sync"));

    public static TardisExteriorSyncPayload create() {
        TardisExterior defaultExterior = TardisExteriorRegistry.getDefault();
        return new TardisExteriorSyncPayload(
                List.copyOf(TardisExteriorRegistry.getAll()),
                defaultExterior != null ? defaultExterior.id() : ""
        );
    }

    public void apply() {
        Set<String> previousIds = GCSyncPayload.idsOf(TardisExteriorRegistry.getAll());

        GCSyncPayload.apply(exteriors, TardisExteriorRegistry::clear, TardisExteriorRegistry::register, "TARDIS Exterior");
        if (defaultId != null && TardisExteriorRegistry.contains(defaultId)) {
            TardisExteriorRegistry.setDefault(TardisExteriorRegistry.get(defaultId));
        }

        GCSyncPayload.reloadIfChanged(previousIds, exteriors);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
