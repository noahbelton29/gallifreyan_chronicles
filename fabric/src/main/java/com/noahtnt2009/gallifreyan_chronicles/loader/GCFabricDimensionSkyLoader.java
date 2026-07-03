package com.noahtnt2009.gallifreyan_chronicles.loader;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyLoader;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.PackType;

public class GCFabricDimensionSkyLoader extends DimensionSkyLoader
        implements PreparableReloadListener {
    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "dimension_sky");

    public static void registerDimensionSkyLoader() {
        Constants.LOG.info("Registered GC Dimension Sky Loader");
        ResourceLoader.get(PackType.SERVER_DATA)
                .registerReloadListener(ID, new GCFabricDimensionSkyLoader());
    }
}