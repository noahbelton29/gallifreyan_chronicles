package uk.nbforge.gallifreyan_chronicles.network;

import uk.nbforge.gallifreyan_chronicles.sky.data.CelestialBody;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.CelestialBodyRegistry;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.DimensionSkyRegistry;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;

public record DimensionSkySyncPayload(List<CelestialBody> bodies,
                                      List<DimensionSky> skies) implements CustomPacketPayload {
    public static final Type<DimensionSkySyncPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("dimension_sky_sync"));

    public static DimensionSkySyncPayload create() {
        return new DimensionSkySyncPayload(List.copyOf(CelestialBodyRegistry.getAll()), List.copyOf(DimensionSkyRegistry.getAll()));
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
