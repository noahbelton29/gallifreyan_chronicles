package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public final class TardisAutopilot {
    private static final int CHECK_TICKS = 20;
    private static final int UNOCCUPIED_TICKS_BEFORE_LANDING = 20 * 60;

    private static final Set<UUID> FLYING = ServerState.track(new HashSet<>());
    private static final Map<UUID, Integer> UNOCCUPIED_TICKS = ServerState.track(new HashMap<>());

    private TardisAutopilot() {}

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
        SchedulerUtil.runLater(server, CHECK_TICKS, () -> tick(server, id));
    }

    private static void tick(MinecraftServer server, UUID id) {
        Tardis tardis = Tardis.get(server, id).orElse(null);
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
        if (ticks >= UNOCCUPIED_TICKS_BEFORE_LANDING && tardis.land()) {
            FLYING.remove(id);
            UNOCCUPIED_TICKS.remove(id);
            return;
        }
        schedule(server, id);
    }

    private static boolean occupied(Tardis tardis) {
        ServerLevel interior = tardis.interiorLevel().orElse(null);
        return interior != null && !interior.players().isEmpty();
    }
}
