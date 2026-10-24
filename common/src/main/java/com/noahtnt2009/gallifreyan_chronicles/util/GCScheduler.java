package com.noahtnt2009.gallifreyan_chronicles.util;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GCScheduler {
    private record Task(MinecraftServer server, int[] ticksLeft, Runnable action) {}

    private static final List<Task> TASKS = new ArrayList<>();

    private GCScheduler() {}

    public static void runLater(MinecraftServer server, int delayTicks, Runnable action) {
        TASKS.add(new Task(server, new int[]{Math.max(0, delayTicks)}, action));
    }

    public static void tick(MinecraftServer server) {
        if (TASKS.isEmpty()) return;

        List<Runnable> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (task.server() != server) continue;
            if (--task.ticksLeft()[0] <= 0) {
                due.add(task.action());
                it.remove();
            }
        }

        due.forEach(Runnable::run);
    }

    public static void clear() {
        TASKS.clear();
    }
}
