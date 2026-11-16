package uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem.Refusal;

/**
 * Handles pulling the controls to convince a TARDIS to fly after it refused.
 * Repeated tries make it give in, but it gets more stressed and trusts the pilot less.
 */
public final class TardisConvincing {
    // Pulls inside this time count as quick pulls
    private static final long QUICK_PULL_TICKS = 200L;

    // Without another try for this long it forgets earlier refusals
    private static final long FORGET_TICKS = 600L;

    // How many times it can get more worked up
    private static final int MAX_WORKED_UP = 3;

    // Stress added for each level of worked up
    private static final float WORKED_UP_STRESS = 5f;

    // Trust lost each time it gets worked up
    private static final float WORKED_UP_TRUST_LOSS = 1f;

    // Base trust lost when the pilot forces it to fly
    private static final float FORCED_TRUST_LOSS = 3f;

    /**
     * How close it is to giving in.
     * refusals is the pulls in this try, quickPullUntil is the time until another pull counts as quick, lastBalk is the last refusal and workedUp is how worked up it is.
     */
    private record State(int refusals, long quickPullUntil, long lastBalk, int workedUp) {}

    private static final Map<UUID, State> STATES = new HashMap<>();

    private TardisConvincing() {}

    public static void reset() {
        STATES.clear();
    }

    public static void forget(Tardis tardis) {
        STATES.remove(tardis.id());
    }

    /**
     * The first pull is refused, more quick pulls raise the refusal count until it gives in.
     * Waiting too long makes it worked up instead.
     */
    public static Refusal pull(Tardis tardis, @Nullable UUID pilot) {
        long now = TardisMindSystem.now(tardis.server());
        State state = current(tardis, now);

        if (state == null) {
            STATES.put(tardis.id(), new State(1, now + QUICK_PULL_TICKS, now, 0));
            return Refusal.BALK;
        }

        if (now > state.quickPullUntil()) {
            return getWorkedUp(tardis, pilot, state, now);
        }

        if (state.refusals() >= pullsNeeded(tardis, state)) {
            return giveIn(tardis, pilot, state);
        }

        STATES.put(tardis.id(), new State(state.refusals() + 1, now + QUICK_PULL_TICKS, now, state.workedUp()));
        return state.workedUp() > 0 ? Refusal.WORKED_UP : Refusal.BALK;
    }

    /** The state, or null once it has expired. */
    private static State current(Tardis tardis, long now) {
        State state = STATES.get(tardis.id());
        if (state == null || now - state.lastBalk() > FORGET_TICKS) return null;
        return state;
    }

    /** Waiting too long between pulls makes it more worked up. */
    private static Refusal getWorkedUp(Tardis tardis, @Nullable UUID pilot, State state, long now) {
        int workedUp = Math.min(state.workedUp() + 1, MAX_WORKED_UP);
        TardisMindSystem.addStress(tardis, WORKED_UP_STRESS * workedUp);
        TardisMindSystem.addTrust(tardis, pilot, -WORKED_UP_TRUST_LOSS);
        STATES.put(tardis.id(), new State(1, now + QUICK_PULL_TICKS, now, workedUp));
        return Refusal.WORKED_UP;
    }

    /** The more worked up it is, the more trust is lost for forcing it to fly. */
    private static Refusal giveIn(Tardis tardis, @Nullable UUID pilot, State state) {
        forget(tardis);
        TardisMindSystem.addTrust(tardis, pilot, -FORCED_TRUST_LOSS * (1 + state.workedUp()));
        return Refusal.NONE;
    }

    /** A positive refusal shift raises the base number of pulls needed. */
    private static int pullsNeeded(Tardis tardis, State state) {
        int base = TardisMindSystem.mind(tardis).modifiers().refusalShift() > 0f ? 2 : 1;
        return base + state.workedUp();
    }
}
