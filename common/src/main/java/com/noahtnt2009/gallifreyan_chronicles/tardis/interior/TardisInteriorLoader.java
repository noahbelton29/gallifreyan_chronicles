package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import net.minecraft.server.MinecraftServer;

public class TardisInteriorLoader extends GCJsonReloadListener<TardisInterior> {
    public TardisInteriorLoader() {
        super(TardisInterior.CODEC, "tardis_interior", "TARDIS Interior");
    }

    @Override
    protected void clearRegistry() {
        TardisInteriorRegistry.clear();
    }

    @Override
    protected void register(TardisInterior value) {
        TardisInteriorRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return TardisInteriorRegistry.size();
    }

    @Override
    protected void onReloadComplete() {
        if (!TardisInteriorRegistry.contains(TardisInteriorRegistry.defaultId())) {
            Constants.LOG.warn("Default TARDIS interior {} not found; expected data/{}/tardis_interior/first_doctors_interior.json",
                    TardisInteriorRegistry.defaultId(), Constants.MOD_ID);
        }
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        // Interiors are server-side only.
    }
}
