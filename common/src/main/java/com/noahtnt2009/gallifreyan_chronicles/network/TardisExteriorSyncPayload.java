package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
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

public record TardisExteriorSyncPayload(List<TardisExterior> exteriors, String defaultId) implements CustomPacketPayload {
    public static final Type<TardisExteriorSyncPayload> TYPE =
            new Type<>(GCUtils.of("tardis_exterior_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisExteriorSyncPayload> STREAM_CODEC = StreamCodec.composite(
            TardisExterior.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisExteriorSyncPayload::exteriors,
            ByteBufCodecs.STRING_UTF8,
            TardisExteriorSyncPayload::defaultId,
            TardisExteriorSyncPayload::new
    );

    public static TardisExteriorSyncPayload create() {
        TardisExterior defaultExterior = TardisExteriorRegistry.getDefault();
        return new TardisExteriorSyncPayload(
                List.copyOf(TardisExteriorRegistry.getAll()),
                defaultExterior != null ? defaultExterior.id() : ""
        );
    }

    public void apply() {
        Set<String> previousIds = TardisExteriorRegistry.getAll().stream()
                .map(TardisExterior::id)
                .collect(Collectors.toSet());

        GCSyncPayload.apply(exteriors, TardisExteriorRegistry::clear, TardisExteriorRegistry::register, "TARDIS Exterior");
        if (defaultId != null && TardisExteriorRegistry.contains(defaultId)) {
            TardisExteriorRegistry.setDefault(TardisExteriorRegistry.get(defaultId));
        }

        Set<String> newIds = exteriors.stream()
                .map(TardisExterior::id)
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
