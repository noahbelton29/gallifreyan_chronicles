package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;

public class GCExteriors {
    public static final TardisExterior POLICE_BOX_1963 =
            TardisExteriorRegistry.register(new TardisExterior("1963_police_box"));
    public static final TardisExterior POLICE_BOX_1966 =
            TardisExteriorRegistry.register(new TardisExterior("1966_police_box"));
    public static final TardisExterior CORRUPTED =
            TardisExteriorRegistry.register(new TardisExterior("corrupted"));


    public static void init() {
        TardisExteriorRegistry.setDefault(CORRUPTED);
    }
}