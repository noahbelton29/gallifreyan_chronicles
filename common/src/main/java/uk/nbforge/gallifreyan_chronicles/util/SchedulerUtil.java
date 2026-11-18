package uk.nbforge.gallifreyan_chronicles.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;

public final class SchedulerUtil {
    private record Task(MinecraftServer server, int[] ticksLeft, Runnable action) {}

    private static final List<Task> TASKS = ServerState.track(new ArrayList<>());

    private SchedulerUtil() {}

    public static void runLater(MinecraftServer server, int delayTicks, Runnable action) {
        TASKS.add(new Task(server, new int[] {Math.max(0, delayTicks)}, action));
    }

    public static void tick(MinecraftServer server) {
        if (TASKS.isEmpty()) return;

        List<Runnable> due = null;
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (task.server() != server) continue;
            if (--task.ticksLeft()[0] <= 0) {
                if (due == null) due = new ArrayList<>();
                due.add(task.action());
                it.remove();
            }
        }

        if (due == null) return;
        for (Runnable action : due) {
            action.run();
        }
    }

    public static void clear() {
        TASKS.clear();
    }
}
