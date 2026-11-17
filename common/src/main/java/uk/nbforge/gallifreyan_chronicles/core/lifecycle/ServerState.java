package uk.nbforge.gallifreyan_chronicles.core.lifecycle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Game state that belongs to the running server, like which TARDISes are crashing or which alarms are ringing.
 * Wrap a static collection in {@code ServerState.track(...)} and it is emptied automatically when the server stops,
 * so nothing carries over into the next world opened in singleplayer.
 */
public final class ServerState {
    private static final List<Runnable> CLEARERS = new CopyOnWriteArrayList<>();

    private ServerState() {}

    public static <T extends Map<?, ?>> T track(T map) {
        CLEARERS.add(map::clear);
        return map;
    }

    public static <T extends Collection<?>> T track(T collection) {
        CLEARERS.add(collection::clear);
        return collection;
    }

    /**
     * Empties every tracked collection. Called when the server stops.
     */
    public static void clearAll() {
        for (Runnable clearer : CLEARERS) {
            clearer.run();
        }
    }
}
