package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyRegistry;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record DimensionSkySyncPayload(List<DimensionSky> skies) implements CustomPacketPayload {
    public static final Type<DimensionSkySyncPayload> TYPE =
            new Type<>(GCUtils.of("dimension_sky_sync"));

    public static final StreamCodec<FriendlyByteBuf, DimensionSkySyncPayload> STREAM_CODEC = StreamCodec.composite(
            DimensionSky.STREAM_CODEC.apply(ByteBufCodecs.list()), DimensionSkySyncPayload::skies,
            DimensionSkySyncPayload::new
    );

    public static DimensionSkySyncPayload create() {
        return new DimensionSkySyncPayload(List.copyOf(DimensionSkyRegistry.getAll()));
    }

    public void apply() {
        Set<String> previousIds = DimensionSkyRegistry.getAll().stream()
                .map(DimensionSky::id)
                .collect(Collectors.toSet());

        GCSyncPayload.apply(skies, DimensionSkyRegistry::clear, DimensionSkyRegistry::register, "Dimension Sky");

        Set<String> newIds = skies.stream()
                .map(DimensionSky::id)
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
