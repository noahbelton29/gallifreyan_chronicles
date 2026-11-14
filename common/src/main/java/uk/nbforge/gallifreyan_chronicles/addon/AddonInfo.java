package uk.nbforge.gallifreyan_chronicles.addon;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record AddonInfo(String id, String name, String version, String description, List<String> authors,
                        @Nullable String website, Path path, boolean hasData, @Nullable String javaMain,
                        byte @Nullable [] icon, int apiVersion, Map<String, String> requires,
                        @Nullable String sha256, @Nullable String error) {
    public boolean hasJava() {
        return javaMain != null;
    }

    public boolean isInvalid() {
        return error != null;
    }
}
