package uk.nbforge.gallifreyan_chronicles.tardis;


import java.util.Comparator;
import java.util.List;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisInteriorRegistry;

public final class TardisInteriorOptions {
    public record Option(String interiorId, String exteriorId) {
    }

    private static volatile List<Option> clientOptions = List.of();
    private static volatile String clientDefaultId = "";

    private TardisInteriorOptions() {
    }

    public static List<Option> fromRegistry() {
        return TardisInteriorRegistry.getAll().stream()
                .sorted(Comparator.comparing(TardisInterior::id))
                .map(interior -> new Option(interior.id(), interior.resolveExteriorId()))
                .toList();
    }

    public static void setClient(List<Option> options, String defaultId) {
        clientOptions = List.copyOf(options);
        clientDefaultId = defaultId == null ? "" : defaultId;
    }

    public static List<Option> client() {
        return clientOptions;
    }

    public static String clientDefaultId() {
        return clientDefaultId;
    }

    public static String effectiveInteriorId(String stored) {
        if (stored != null) return stored;
        if (!clientDefaultId.isEmpty()) return clientDefaultId;
        return clientOptions.isEmpty() ? "" : clientOptions.getFirst().interiorId();
    }

    public static Option find(String interiorId) {
        for (Option option : clientOptions) {
            if (option.interiorId().equals(interiorId)) return option;
        }
        return null;
    }
}
