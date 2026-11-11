package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal;

import org.jspecify.annotations.Nullable;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TardisAnger {
    private static final long FORGET_TICKS = 600L;
    private static final int MAX_LEVEL = 8;
    private static final float TRUST_LOSS = 1f;

    private record State(int level, long lastPull) {
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private TardisAnger() {
    }

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

    public static int level(Tardis tardis) {
        State state = STATES.get(tardis.id());
        if (state == null) return 0;
        boolean forgotten = TardisMindSystem.now(tardis.server()) - state.lastPull() > FORGET_TICKS;
        return forgotten ? 0 : state.level();
    }
}
