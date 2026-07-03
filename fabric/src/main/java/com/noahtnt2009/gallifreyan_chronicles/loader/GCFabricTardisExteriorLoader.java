package com.noahtnt2009.gallifreyan_chronicles.loader;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.PackType;

public class GCFabricTardisExteriorLoader extends TardisExteriorLoader
        implements PreparableReloadListener {
    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "tardis_exteriors");

    public static void registerTardisExteriorLoader() {
        Constants.LOG.info("Registered GC Exterior Loader");
        ResourceLoader.get(PackType.SERVER_DATA)
                .registerReloadListener(ID, new GCFabricTardisExteriorLoader());
    }
}