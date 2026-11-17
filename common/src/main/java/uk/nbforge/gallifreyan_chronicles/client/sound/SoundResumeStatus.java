package uk.nbforge.gallifreyan_chronicles.client.sound;

import java.util.concurrent.atomic.AtomicBoolean;
import uk.nbforge.gallifreyan_chronicles.Constants;

public final class SoundResumeStatus {
    private static final AtomicBoolean ENGINE_HOOKED = new AtomicBoolean();
    private static final AtomicBoolean WARNED = new AtomicBoolean();

    private SoundResumeStatus() {}

    public static void markEngineHooked() {
        ENGINE_HOOKED.set(true);
    }

    public static void warnIfEngineUnhooked() {
        if (!ENGINE_HOOKED.get() && WARNED.compareAndSet(false, true)) {
            Constants.LOG.warn(
                    "The sound resume hook never ran; SoundEngineMixin's target lambda likely changed in this Minecraft version, so resumed sounds will restart from the beginning");
        }
    }
}
