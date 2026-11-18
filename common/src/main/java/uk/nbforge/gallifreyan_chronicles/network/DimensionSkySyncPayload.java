package uk.nbforge.gallifreyan_chronicles.network;

import java.util.List;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.CelestialBodyRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.DimensionSkyRegistry;
import uk.nbforge.gallifreyan_chronicles.sky.data.CelestialBody;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public record DimensionSkySyncPayload(List<CelestialBody> bodies, List<DimensionSky> skies)
        implements CustomPacketPayload {
    public static final Type<DimensionSkySyncPayload> TYPE = new Type<>(GCUtil.modIdentifier("dimension_sky_sync"));

    public static final StreamCodec<FriendlyByteBuf, DimensionSkySyncPayload> STREAM_CODEC = StreamCodec.composite(
            CelestialBody.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DimensionSkySyncPayload::bodies,
            DimensionSky.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DimensionSkySyncPayload::skies,
            DimensionSkySyncPayload::new);

    public static DimensionSkySyncPayload create() {
        return new DimensionSkySyncPayload(
                List.copyOf(CelestialBodyRegistry.getAll()), List.copyOf(DimensionSkyRegistry.getAll()));
    }

    public void apply() {
        Set<String> previousIds = GCSyncPayload.idsOf(DimensionSkyRegistry.getAll());

        GCSyncPayload.apply(bodies, CelestialBodyRegistry::clear, CelestialBodyRegistry::register, "Celestial Body");
        GCSyncPayload.apply(skies, DimensionSkyRegistry::clear, DimensionSkyRegistry::register, "Dimension Sky");

        GCSyncPayload.reloadIfChanged(previousIds, skies);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
