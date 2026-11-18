package uk.nbforge.gallifreyan_chronicles.client.gui.hud;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import uk.nbforge.gallifreyan_chronicles.client.lifecycle.ClientState;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisAlarmPayload;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;

public final class DangerAlarm {
    private static final long FLASH_NANOS = TardisAlarm.FLASH_TICKS * 50_000_000L;
    private static final Map<ResourceKey<Level>, State> ALARMS = ClientState.track(new HashMap<>());
    private static final long FOREVER_TIMEOUT_NANOS = FLASH_NANOS * 3;

    private static final class State {
        int flashes;
        long startNanos;
        long lastHeardNanos;
    }

    private DangerAlarm() {}

    public static void start(int count, ResourceKey<Level> dimension) {
        if (count == TardisAlarmPayload.STOP) {
            ALARMS.remove(dimension);
            return;
        }
        State state = ALARMS.get(dimension);
        long now = System.nanoTime();
        boolean alreadyForever = state != null && state.flashes == TardisAlarmPayload.FOREVER;
        if (alreadyForever && count != TardisAlarmPayload.FOREVER) return;
        if (alreadyForever) {
            state.lastHeardNanos = now;
            return;
        }
        if (state == null) {
            state = new State();
            ALARMS.put(dimension, state);
        }
        state.flashes = count;
        state.startNanos = now;
        state.lastHeardNanos = now;
    }

    public static void reset() {
        ALARMS.clear();
    }

    public static float amountFor(Level level) {
        if (level == null) return 0.0f;
        State state = ALARMS.get(level.dimension());
        if (state == null) return 0.0f;
        long now = System.nanoTime();
        if (state.flashes == TardisAlarmPayload.FOREVER && now - state.lastHeardNanos > FOREVER_TIMEOUT_NANOS) {
            ALARMS.remove(level.dimension());
            return 0.0f;
        }
        long passed = now - state.startNanos;
        if (state.flashes > 0 && passed >= FLASH_NANOS * state.flashes) {
            ALARMS.remove(level.dimension());
            return 0.0f;
        }
        float t = (passed % FLASH_NANOS) / (float) FLASH_NANOS;
        return (float) Math.sin(Math.PI * t);
    }
}
