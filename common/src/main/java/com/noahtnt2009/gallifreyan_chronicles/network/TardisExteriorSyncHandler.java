package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;

public final class TardisExteriorSyncHandler {
    private TardisExteriorSyncHandler() {
    }

    public static void apply(TardisExteriorSyncPayload payload) {
        TardisExteriorRegistry.clear();
        for (TardisExterior exterior : payload.exteriors()) {
            TardisExteriorRegistry.register(exterior);
        }
        if (payload.defaultId() != null && TardisExteriorRegistry.contains(payload.defaultId())) {
            TardisExteriorRegistry.setDefault(TardisExteriorRegistry.get(payload.defaultId()));
        }
        Constants.LOG.info("Synced {} TARDIS exterior(s) to client", payload.exteriors().size());
    }
}
