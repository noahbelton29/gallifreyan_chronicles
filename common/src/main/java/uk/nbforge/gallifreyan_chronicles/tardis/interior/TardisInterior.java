package uk.nbforge.gallifreyan_chronicles.tardis.interior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.loader.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public record TardisInterior(String id, Optional<String> exterior) implements IdentifiableData<TardisInterior> {

    public static final TardisInterior FALLBACK = new TardisInterior("template_interior", Optional.empty());

    public static final Codec<TardisInterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(TardisInterior::id),
                    Codec.STRING.optionalFieldOf("exterior").forGetter(TardisInterior::exterior))
            .apply(inst, TardisInterior::new));

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
