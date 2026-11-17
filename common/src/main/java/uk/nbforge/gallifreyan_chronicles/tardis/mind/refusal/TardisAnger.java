package uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;

/**
 * How angry a TARDIS is after being provoked.
 * Anger grows with each provocation, adding stress and lowering the trust of the pilot, and it fades if the TARDIS is left alone.
 */
public final class TardisAnger {
    private static final long FORGET_TICKS = 600L;
    private static final int MAX_LEVEL = 8;
    private static final float TRUST_LOSS = 1f;

    private record State(int level, long lastPull) {}

    private static final Map<UUID, State> STATES = ServerState.track(new HashMap<>());

    private TardisAnger() {}

    public static void reset() {
        STATES.clear();
    }

    public static void forget(Tardis tardis) {
        STATES.remove(tardis.id());
    }

    public static void provoke(Tardis tardis, float stressPerLevel, @Nullable UUID pilot) {
        int level = Math.min(level(tardis) + 1, MAX_LEVEL);
        STATES.put(tardis.id(), new State(level, TardisMindSystem.now(tardis.server())));
        TardisMindSystem.addStress(tardis, stressPerLevel * level);
        TardisMindSystem.addTrust(tardis, pilot, -TRUST_LOSS);
    }

    /** Anger is forgotten if it is not provoked again within FORGET_TICKS ticks. */
    public static int level(Tardis tardis) {
        State state = STATES.get(tardis.id());
        if (state == null) return 0;

        boolean forgotten = TardisMindSystem.now(tardis.server()) - state.lastPull() > FORGET_TICKS;
        return forgotten ? 0 : state.level();
    }
}
