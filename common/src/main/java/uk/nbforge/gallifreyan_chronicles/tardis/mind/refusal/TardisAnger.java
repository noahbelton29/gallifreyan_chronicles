
package uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal;

import org.jspecify.annotations.Nullable;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks how angry a TARDIS is after being provoked.
 *
 * <p>Anger increases with each provocation, adding stress and reducing the
 * pilot's trust. It eventually fades if the TARDIS is left alone.
 */
public final class TardisAnger {
    private static final long FORGET_TICKS = 600L;
    private static final int MAX_LEVEL = 8;
    private static final float TRUST_LOSS = 1f;

    private record State(int level, long lastPull) {
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private TardisAnger() {
    }

    /**
     * Clears all tracked anger states.
     */
    public static void reset() {
        STATES.clear();
    }

    /**
     * Removes the anger state for a TARDIS.
     */
    public static void forget(Tardis tardis) {
        STATES.remove(tardis.id());
    }

    /**
     * Increases a TARDIS's anger and applies the resulting stress and trust changes.
     *
     * @param tardis         the TARDIS being provoked
     * @param stressPerLevel the amount of stress added per anger level
     * @param pilot          the player responsible for the provocation, if any
     */
    public static void provoke(Tardis tardis, float stressPerLevel, @Nullable UUID pilot) {
        int level = Math.min(level(tardis) + 1, MAX_LEVEL);
        STATES.put(tardis.id(), new State(level, TardisMindSystem.now(tardis.server())));
        TardisMindSystem.addStress(tardis, stressPerLevel * level);
        TardisMindSystem.addTrust(tardis, pilot, -TRUST_LOSS);
    }

    /**
     * Gets the current anger level of a TARDIS.
     *
     * <p>Anger is considered forgotten if it has not been provoked again
     * within {@link #FORGET_TICKS} ticks.
     *
     * @param tardis the TARDIS to check
     * @return the current anger level, or {@code 0} if its anger has faded
     */
    public static int level(Tardis tardis) {
        State state = STATES.get(tardis.id());
        if (state == null) return 0;

        boolean forgotten = TardisMindSystem.now(tardis.server()) - state.lastPull() > FORGET_TICKS;
        return forgotten ? 0 : state.level();
    }
}
