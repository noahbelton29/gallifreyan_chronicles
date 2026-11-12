package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal;

import org.jspecify.annotations.Nullable;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem.Refusal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles attempts to convince a TARDIS to fly after it has refused.
 * <p>
 * Tracks repeated attempts to pull the controls, allowing the TARDIS to
 * eventually give in while increasing its stress and losing trust in the pilot.
 */
public final class TardisConvincing {
    /**
     * Time window in which repeated pulls count towards convincing the TARDIS.
     */
    private static final long QUICK_PULL_TICKS = 200L;

    /**
     * Time without another attempt before the TARDIS forgets previous refusals.
     */
    private static final long FORGET_TICKS = 600L;

    /**
     * Maximum number of times the TARDIS can become increasingly worked up.
     */
    private static final int MAX_WORKED_UP = 3;

    /**
     * Stress added for each level of the TARDIS becoming worked up.
     */
    private static final float WORKED_UP_STRESS = 5f;

    /**
     * Trust lost each time the TARDIS becomes worked up.
     */
    private static final float WORKED_UP_TRUST_LOSS = 1f;

    /**
     * Base trust lost when the pilot forces the TARDIS to fly.
     */
    private static final float FORCED_TRUST_LOSS = 3f;

    /**
     * Tracks a TARDIS's progress towards giving in to repeated attempts.
     *
     * @param refusals       number of pulls made during the current convincing attempt
     * @param quickPullUntil time until another pull counts as a quick pull
     * @param lastBalk       time of the most recent refusal
     * @param workedUp       how worked up the TARDIS has become
     */
    private record State(int refusals, long quickPullUntil, long lastBalk, int workedUp) {
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private TardisConvincing() {
    }

    public static void reset() {
        STATES.clear();
    }

    public static void forget(Tardis tardis) {
        STATES.remove(tardis.id());
    }

    /**
     * Processes another attempt to pull the TARDIS controls.
     * <p>
     * A first pull causes a refusal. Further pulls within the quick-pull
     * window increase the refusal count until the TARDIS gives in.
     * Waiting too long causes the TARDIS to become worked up instead.
     *
     * @param tardis the TARDIS being convinced
     * @param pilot  UUID of the player attempting to fly, or {@code null}
     * @return the TARDIS's resulting refusal state
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

    /**
     * Gets the current convincing state if it has not expired.
     *
     * @param tardis the TARDIS whose state is being checked
     * @param now    the current game time
     * @return the current state, or {@code null} if it has expired
     */
    private static State current(Tardis tardis, long now) {
        State state = STATES.get(tardis.id());
        if (state == null || now - state.lastBalk() > FORGET_TICKS) return null;
        return state;
    }

    /**
     * Increases how worked up the TARDIS becomes when the pilot waits too long
     * between pulls.
     *
     * @param tardis the TARDIS being convinced
     * @param pilot  UUID of the player attempting to fly, or {@code null}
     * @param state  the previous convincing state
     * @param now    the current game time
     * @return {@link Refusal#WORKED_UP}
     */
    private static Refusal getWorkedUp(Tardis tardis, @Nullable UUID pilot, State state, long now) {
        int workedUp = Math.min(state.workedUp() + 1, MAX_WORKED_UP);
        TardisMindSystem.addStress(tardis, WORKED_UP_STRESS * workedUp);
        TardisMindSystem.addTrust(tardis, pilot, -WORKED_UP_TRUST_LOSS);
        STATES.put(tardis.id(), new State(1, now + QUICK_PULL_TICKS, now, workedUp));
        return Refusal.WORKED_UP;
    }

    /**
     * Makes the TARDIS give in to the pilot's repeated attempts.
     * <p>
     * The more worked up the TARDIS has become, the greater the trust loss
     * from forcing it to fly.
     *
     * @param tardis the TARDIS being convinced
     * @param pilot  UUID of the player attempting to fly, or {@code null}
     * @param state  the current convincing state
     * @return {@link Refusal#NONE}
     */
    private static Refusal giveIn(Tardis tardis, @Nullable UUID pilot, State state) {
        forget(tardis);
        TardisMindSystem.addTrust(tardis, pilot, -FORCED_TRUST_LOSS * (1 + state.workedUp()));
        return Refusal.NONE;
    }

    /**
     * Calculates how many pulls are needed before the TARDIS gives in.
     * <p>
     * A positive refusal shift modifier increases the base requirement.
     *
     * @param tardis the TARDIS being convinced
     * @param state  the current convincing state
     * @return the number of required pulls
     */
    private static int pullsNeeded(Tardis tardis, State state) {
        int base = TardisMindSystem.mind(tardis).modifiers().refusalShift() > 0f ? 2 : 1;
        return base + state.workedUp();
    }
}