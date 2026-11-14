package uk.nbforge.gallifreyan_chronicles.tardis.console;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record ControlAnimationSetting(boolean enabled, Optional<String> active, Optional<String> inactive,
                                      Optional<Boolean> snapWhenInactive) {
    public static final ControlAnimationSetting DEFAULT =
            new ControlAnimationSetting(true, Optional.empty(), Optional.empty(), Optional.empty());
    public static final ControlAnimationSetting DISABLED =
            new ControlAnimationSetting(false, Optional.empty(), Optional.empty(), Optional.empty());

    public @Nullable String animationFor(boolean activated, @Nullable String fallback) {
        if (!enabled) return null;
        return (activated ? active : inactive).orElse(fallback);
    }

    public boolean snapsWhenInactive(boolean fallback) {
        return snapWhenInactive.orElse(fallback);
    }
}
