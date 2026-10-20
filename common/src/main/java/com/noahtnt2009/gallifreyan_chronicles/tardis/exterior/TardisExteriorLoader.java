package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

public class TardisExteriorLoader extends GCJsonReloadListener<TardisExterior> {
    public TardisExteriorLoader() {
        super(TardisExterior.CODEC, "tardis_exterior", "TARDIS Exterior");
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
        TardisExterior def = TardisExteriorRegistry.get(TardisExteriorRegistry.defaultId());
        if (def == null) {
            Constants.LOG.warn("No TARDIS exteriors loaded! Expected e.g. data/{}/tardis_exterior/first_doctors_exterior.json", Constants.MOD_ID);
            return;
        }
        TardisExteriorRegistry.setDefault(def);
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastTardisExteriorSync(server, TardisExteriorSyncPayload.create());
    }
}
