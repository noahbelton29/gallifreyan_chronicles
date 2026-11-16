package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
   A TARDIS left flying with nobody aboard lands itself at wherever it was last headed, rather than
   drifting through the vortex empty and unsupervised forever (left mid flight by a player logging
   off, a crashed client, or anything else that empties the console room).
*/
public final class TardisAutopilot {
    private static final int CHECK_TICKS = 20;
    private static final int UNOCCUPIED_TICKS_BEFORE_LANDING = 20 * 60;

    private static final Set<UUID> FLYING = new HashSet<>();
    private static final Map<UUID, Integer> UNOCCUPIED_TICKS = new HashMap<>();

    private TardisAutopilot() {
    }

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisAutopilot::onTakeOff);
    }

    public static void reset() {
        FLYING.clear();
        UNOCCUPIED_TICKS.clear();
    }

    private static void onTakeOff(Tardis tardis) {
        if (FLYING.add(tardis.id())) schedule(tardis.server(), tardis.id());
    }

    private static void schedule(MinecraftServer server, UUID id) {
        GCSchedulerUtil.runLater(server, CHECK_TICKS, () -> tick(server, id));
    }

    private static void tick(MinecraftServer server, UUID id) {
        Tardis tardis = TardisApi.get(server, id).orElse(null);
        boolean flying = tardis != null && tardis.isTravelling() && !TardisCrash.isCrashing(tardis);
        if (!flying) {
            FLYING.remove(id);
            UNOCCUPIED_TICKS.remove(id);
            return;
        }

        if (occupied(tardis)) {
            UNOCCUPIED_TICKS.remove(id);
            schedule(server, id);
            return;
        }

        int ticks = UNOCCUPIED_TICKS.merge(id, CHECK_TICKS, Integer::sum);
        // Once past the threshold, keep trying every check until land() actually succeeds, it is a
        // no-op while still mid takeoff/landing or before the minimum travel time has passed
        if (ticks < UNOCCUPIED_TICKS_BEFORE_LANDING || !tardis.land()) {
            schedule(server, id);
            return;
        }

        FLYING.remove(id);
        UNOCCUPIED_TICKS.remove(id);
    }

    private static boolean occupied(Tardis tardis) {
        ServerLevel interior = tardis.interiorLevel().orElse(null);
        return interior != null && !interior.players().isEmpty();
    }
}
