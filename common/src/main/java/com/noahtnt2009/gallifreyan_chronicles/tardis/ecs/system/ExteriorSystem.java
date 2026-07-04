package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.ExteriorComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;

public final class ExteriorSystem {
    private ExteriorSystem() {
    }

    public static TardisExterior exteriorOf(Entity entity) {
        return entity.get(TardisComponentTypes.EXTERIOR).exterior();
    }

    public static void setExterior(Entity entity, TardisExterior exterior) {
        entity.set(TardisComponentTypes.EXTERIOR, ExteriorComponent.of(exterior));
    }
}
