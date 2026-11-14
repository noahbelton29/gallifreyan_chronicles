package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisExteriorRegistry;

public record ExteriorComponent(String exteriorId) implements Component {
    public static ExteriorComponent ofDefault() {
        return new ExteriorComponent(TardisExteriorRegistry.getDefault().id());
    }

    public static ExteriorComponent of(TardisExterior exterior) {
        return new ExteriorComponent(exterior.id());
    }

    public TardisExterior exterior() {
        return TardisExteriorRegistry.get(exteriorId);
    }
}
