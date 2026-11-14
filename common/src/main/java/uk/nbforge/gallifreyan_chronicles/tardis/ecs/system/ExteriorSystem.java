package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import uk.nbforge.gallifreyan_chronicles.ecs.Entity;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.ExteriorComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;

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
