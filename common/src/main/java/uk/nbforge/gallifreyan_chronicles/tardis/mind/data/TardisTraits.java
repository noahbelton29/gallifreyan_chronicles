package uk.nbforge.gallifreyan_chronicles.tardis.mind.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;

public final class TardisTraits {
    private static final Map<String, TardisTrait> ALL = new ConcurrentSkipListMap<>();

    private TardisTraits() {}

    public static void clear() {
        ALL.clear();
    }

    public static void register(TardisTrait trait) {
        ALL.put(trait.id(), trait);
    }

    public static int size() {
        return ALL.size();
    }

    public static Collection<TardisTrait> all() {
        return ALL.values();
    }

    public static Optional<TardisTrait> byId(String id) {
        return Optional.ofNullable(ALL.get(MindIds.normalize(id)));
    }

    /** traits a datapack removed just get skipped */
    public static List<TardisTrait> of(List<String> ids) {
        return ids.stream().map(TardisTraits::byId).flatMap(Optional::stream).toList();
    }
}
