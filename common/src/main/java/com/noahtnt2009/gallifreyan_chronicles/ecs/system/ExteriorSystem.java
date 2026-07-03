package com.noahtnt2009.gallifreyan_chronicles.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.ecs.component.ComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.ecs.component.ExteriorComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;

public final class ExteriorSystem {
    private ExteriorSystem() {
    }

    public static TardisExterior exteriorOf(Entity entity) {
        return entity.get(ComponentTypes.EXTERIOR).exterior();
    }

    public static void setExterior(Entity entity, TardisExterior exterior) {
        entity.set(ComponentTypes.EXTERIOR, ExteriorComponent.of(exterior));
    }
}
