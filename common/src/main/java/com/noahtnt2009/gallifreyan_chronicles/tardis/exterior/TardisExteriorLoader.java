package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

public class TardisExteriorLoader extends GCJsonReloadListener<TardisExterior> {
    public TardisExteriorLoader() {
        super(TardisExterior.CODEC, "tardis_exteriors", "TARDIS Exterior");
    }

    @Override
    protected void clearRegistry() {
        TardisExteriorRegistry.clear();
    }

    @Override
    protected void register(TardisExterior value) {
        TardisExteriorRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisExteriorRegistry.size();
    }

    @Override
    protected void onReloadComplete() {
        TardisExteriorRegistry.setDefault(TardisExteriorRegistry.get(TardisExteriorRegistry.DEFAULT_ID));
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisExteriorSync(server, TardisExteriorSyncPayload.create());
    }
}
