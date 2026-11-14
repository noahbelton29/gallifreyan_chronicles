package uk.nbforge.gallifreyan_chronicles.tardis;

import uk.nbforge.gallifreyan_chronicles.Constants;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class TardisEvents {
    private TardisEvents() {
    }

    public static final class Event {
        private final List<Consumer<Tardis>> listeners = new CopyOnWriteArrayList<>();

        public void register(Consumer<Tardis> listener) {
            listeners.add(listener);
        }

        public void unregister(Consumer<Tardis> listener) {
            listeners.remove(listener);
        }

        public void fire(Tardis tardis) {
            for (Consumer<Tardis> listener : listeners) {
                try {
                    listener.accept(tardis);
                } catch (RuntimeException e) {
                    Constants.LOG.error("TARDIS event listener failed", e);
                }
            }
        }
    }

    public static final Event TAKE_OFF = new Event();
    public static final Event LAND = new Event();
}
