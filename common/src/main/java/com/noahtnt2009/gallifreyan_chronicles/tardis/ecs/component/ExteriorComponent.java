package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;

public record ExteriorComponent(String exteriorId) implements Component {
    public static final Codec<ExteriorComponent> CODEC =
            Codec.STRING.xmap(ExteriorComponent::new, ExteriorComponent::exteriorId);

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
