package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncFactory;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class DimensionSkyLoader extends SimpleJsonResourceReloadListener<DimensionSky> {
    private static volatile MinecraftServer currentServer;

    public DimensionSkyLoader() {
        super(DimensionSky.CODEC, FileToIdConverter.json("dimension_sky"));
    }

    public static void setCurrentServer(MinecraftServer server) {
        currentServer = server;
    }

    @Override
    protected void apply(Map<Identifier, DimensionSky> resources, @NonNull ResourceManager manager,
                         @NonNull ProfilerFiller profiler) {
        DimensionSkyRegistry.clear();
        for (Map.Entry<Identifier, DimensionSky> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            String id = fileId.getNamespace() + ":" + fileId.getPath();
            DimensionSky sky = entry.getValue().withId(id);
            DimensionSkyRegistry.register(sky);
            Constants.LOG.debug("Loaded dimension sky: {} for dimension {}", id, sky.dimension());
        }

        MinecraftServer server = currentServer;
        if (server != null) {
            Services.NETWORK.broadcastDimensionSkySync(server, DimensionSkySyncFactory.create());
        }
    }
}