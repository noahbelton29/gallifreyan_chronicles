package uk.nbforge.gallifreyan_chronicles.tardis.console;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public record TardisConsoleVariant(String id, String parent, String texture, Map<String, String> glowTextures)
        implements IdentifiableData<TardisConsoleVariant> {

    public String localId() {
        return GCUtil.localPath(id);
    }

    public @Nullable String glowTexture(String groupName) {
        return glowTextures.get(groupName);
    }

    @Override
    public TardisConsoleVariant withId(String id) {
        return new TardisConsoleVariant(id, parent, texture, glowTextures);
    }
}
