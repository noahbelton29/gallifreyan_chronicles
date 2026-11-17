package uk.nbforge.gallifreyan_chronicles.client.lifecycle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Client-side state that belongs to the world or server the player is connected to, like a ringing alarm,
 * a camera shake or the hums of nearby TARDISes. Wrap a static collection in {@code ClientState.track(...)}
 * and it is emptied automatically when the player leaves a world or disconnects from a server,
 * so nothing carries over into the next one.
 */
public final class ClientState {
    private static final List<Runnable> CLEARERS = new CopyOnWriteArrayList<>();

    private ClientState() {}

    public static <T extends Map<?, ?>> T track(T map) {
        CLEARERS.add(map::clear);
        return map;
    }

    public static <T extends Collection<?>> T track(T collection) {
        CLEARERS.add(collection::clear);
        return collection;
    }

    /**
     * Empties every tracked collection. Called on the client thread when the player disconnects.
     */
    public static void clearAll() {
        for (Runnable clearer : CLEARERS) {
            clearer.run();
        }
    }
}
