package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.tardis.TardisInteriorOptions;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisInteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record TardisInteriorSyncPayload(List<TardisInteriorOptions.Option> options,
                                        String defaultId) implements CustomPacketPayload {
    public static final Type<TardisInteriorSyncPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_interior_sync"));

    public static TardisInteriorSyncPayload create() {
        return new TardisInteriorSyncPayload(TardisInteriorOptions.fromRegistry(), TardisInteriorRegistry.defaultId());
    }

    public void apply() {
        TardisInteriorOptions.setClient(options, defaultId);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
