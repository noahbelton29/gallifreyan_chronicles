package uk.nbforge.gallifreyan_chronicles.compat.flashback;

import io.netty.handler.codec.DecoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public final class ReplayComponentIds {
    public record Payload(List<Integer> ids, List<Identifier> names) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(GCUtil.modIdentifier("replay_component_ids"));

        public static final StreamCodec<FriendlyByteBuf, ReplayComponentIds.Payload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()),
                        ReplayComponentIds.Payload::ids,
                        Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()),
                        ReplayComponentIds.Payload::names,
                        ReplayComponentIds.Payload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static volatile Int2ObjectMap<Identifier> recorded;
    private static final ThreadLocal<Boolean> READING = ThreadLocal.withInitial(() -> false);

    private ReplayComponentIds() {}

    public static Payload current() {
        List<Integer> ids = new ArrayList<>();
        List<Identifier> names = new ArrayList<>();
        for (DataComponentType<?> type : BuiltInRegistries.DATA_COMPONENT_TYPE) {
            Identifier name = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            if (name == null) continue;
            ids.add(BuiltInRegistries.DATA_COMPONENT_TYPE.getId(type));
            names.add(name);
        }
        return new Payload(ids, names);
    }

    public static void use(Payload payload) {
        Int2ObjectMap<Identifier> map = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < payload.ids().size() && i < payload.names().size(); i++) {
            map.put(payload.ids().get(i).intValue(), payload.names().get(i));
        }
        recorded = map;
    }

    public static void clear() {
        recorded = null;
    }

    public static void startReading() {
        READING.set(true);
    }

    public static void stopReading() {
        READING.set(false);
    }

    public static boolean translating() {
        return recorded != null && READING.get();
    }

    public static DataComponentType<?> byRecordedId(int id) {
        Int2ObjectMap<Identifier> map = recorded;
        DataComponentType<?> type = null;
        if (map != null) {
            Identifier name = map.get(id);
            if (name != null) type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(name);
        }
        if (type == null) type = BuiltInRegistries.DATA_COMPONENT_TYPE.byId(id);
        if (type == null) throw new DecoderException("Unknown data component " + id + " in replay");
        return type;
    }
}
