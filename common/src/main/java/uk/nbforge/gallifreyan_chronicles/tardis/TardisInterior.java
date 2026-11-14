package uk.nbforge.gallifreyan_chronicles.tardis;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record TardisInterior(String id, Optional<String> exterior)
        implements IdentifiableData<TardisInterior> {

    public static final TardisInterior FALLBACK = new TardisInterior(
            "template_interior", Optional.empty());

    public Identifier structure() {
        if (id.contains(":")) {
            Identifier parsed = Identifier.tryParse(id);
            if (parsed != null) return parsed;
        }
        return GCUtil.modIdentifier(id);
    }

    public String resolveExteriorId() {
        if (exterior.isPresent()) {
            String id = exterior.get();
            if (!id.contains(":")) id = Constants.MOD_ID + ":" + id;
            if (TardisExteriorRegistry.contains(id)) return id;
        }
        return TardisExteriorRegistry.defaultId();
    }

    @Override
    public TardisInterior withId(String id) {
        return new TardisInterior(id, exterior);
    }
}