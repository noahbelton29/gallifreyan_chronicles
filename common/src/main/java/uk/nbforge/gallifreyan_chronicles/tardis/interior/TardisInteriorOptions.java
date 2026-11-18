package uk.nbforge.gallifreyan_chronicles.tardis.interior;

import io.netty.buffer.ByteBuf;
import java.util.Comparator;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisInteriorRegistry;

public final class TardisInteriorOptions {
    public record Option(String interiorId, String exteriorId) {

        public static final StreamCodec<ByteBuf, TardisInteriorOptions.Option> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                TardisInteriorOptions.Option::interiorId,
                ByteBufCodecs.STRING_UTF8,
                TardisInteriorOptions.Option::exteriorId,
                TardisInteriorOptions.Option::new);
    }

    private static volatile List<Option> clientOptions = List.of();
    private static volatile String clientDefaultId = "";

    private TardisInteriorOptions() {}

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
