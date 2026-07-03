package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;

import java.util.List;

public final class TardisExteriorSyncFactory {
    private TardisExteriorSyncFactory() {
    }

    public static TardisExteriorSyncPayload create() {
        TardisExterior defaultExterior = TardisExteriorRegistry.getDefault();
        return new TardisExteriorSyncPayload(
                List.copyOf(TardisExteriorRegistry.getAll()),
                defaultExterior != null ? defaultExterior.id() : ""
        );
    }
}
