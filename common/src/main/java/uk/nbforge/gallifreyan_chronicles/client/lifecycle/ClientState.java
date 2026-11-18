package uk.nbforge.gallifreyan_chronicles.client.lifecycle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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

    public static void clearAll() {
        for (Runnable clearer : CLEARERS) {
            clearer.run();
        }
    }
}
