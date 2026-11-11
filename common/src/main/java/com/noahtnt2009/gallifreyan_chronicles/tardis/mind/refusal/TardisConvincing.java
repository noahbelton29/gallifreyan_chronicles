package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal;

import org.jspecify.annotations.Nullable;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem.Refusal;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TardisConvincing {
    private static final long QUICK_PULL_TICKS = 200L;
    private static final long FORGET_TICKS = 600L;
    private static final int MAX_WORKED_UP = 3;
    private static final float WORKED_UP_STRESS = 5f;
    private static final float WORKED_UP_TRUST_LOSS = 1f;
    private static final float FORCED_TRUST_LOSS = 3f;

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

    private static State current(Tardis tardis, long now) {
        State state = STATES.get(tardis.id());
        if (state == null || now - state.lastBalk() > FORGET_TICKS) return null;
        return state;
    }

    private static Refusal getWorkedUp(Tardis tardis, @Nullable UUID pilot, State state, long now) {
        int workedUp = Math.min(state.workedUp() + 1, MAX_WORKED_UP);
        TardisMindSystem.addStress(tardis, WORKED_UP_STRESS * workedUp);
        TardisMindSystem.addTrust(tardis, pilot, -WORKED_UP_TRUST_LOSS);
        STATES.put(tardis.id(), new State(1, now + QUICK_PULL_TICKS, now, workedUp));
        return Refusal.WORKED_UP;
    }

    private static Refusal giveIn(Tardis tardis, @Nullable UUID pilot, State state) {
        forget(tardis);
        TardisMindSystem.addTrust(tardis, pilot, -FORCED_TRUST_LOSS * (1 + state.workedUp()));
        return Refusal.NONE;
    }

    private static int pullsNeeded(Tardis tardis, State state) {
        int base = TardisMindSystem.mind(tardis).modifiers().refusalShift() > 0f ? 2 : 1;
        return base + state.workedUp();
    }
}
