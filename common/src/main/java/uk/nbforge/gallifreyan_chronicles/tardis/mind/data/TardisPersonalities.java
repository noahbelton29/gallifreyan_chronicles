package uk.nbforge.gallifreyan_chronicles.tardis.mind.data;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisPersonality;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public final class TardisPersonalities {
    private static final Map<String, TardisPersonality> ALL = new ConcurrentSkipListMap<>();

    private TardisPersonalities() {
    }

    public static void clear() {
        ALL.clear();
    }

    public static void register(TardisPersonality personality) {
        ALL.put(personality.id(), personality);
    }

    public static int size() {
        return ALL.size();
    }

    public static Collection<TardisPersonality> all() {
        return ALL.values();
    }

    public static Optional<TardisPersonality> byId(String id) {
        return Optional.ofNullable(ALL.get(MindIds.normalize(id)));
    }

    /* never null, a removed personality falls back to obedient */
    public static TardisPersonality get(String id) {
        return byId(id).orElseGet(() -> byId(TardisPersonality.FALLBACK.id()).orElse(TardisPersonality.FALLBACK));
    }
}
