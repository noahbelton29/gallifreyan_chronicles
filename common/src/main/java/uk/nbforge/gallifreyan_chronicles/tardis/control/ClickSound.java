package uk.nbforge.gallifreyan_chronicles.tardis.control;

import net.minecraft.sounds.SoundEvent;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;

/** full pitch when the control is on, lower when its off */
public enum ClickSound {
    NONE(0f),
    SLIDER(0.9f),
    SWITCH(0.8f),
    LEVER(0.8f);

    private final float offPitch;

    ClickSound(float offPitch) {
        this.offPitch = offPitch;
    }

    // looked up on use, the sounds aren't registered yet when controls are
    public @Nullable SoundEvent sound() {
        return switch (this) {
            case NONE -> null;
            case SLIDER -> GCSounds.SLIDER;
            case SWITCH -> GCSounds.SWITCH;
            case LEVER -> GCSounds.LEVER;
        };
    }

    public float offPitch() {
        return offPitch;
    }
}
