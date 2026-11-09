package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
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

/* item data in packets names each component by a number (its slot in the registry),
   not by its name. the world you record in can number them differently than a fresh
   replay does (fabric keeps each world's own order), so the replay read the wrong
   component: "unknown item info tooltip: tuat_interior", "not a string". we save
   the recording's numbers in the snapshot and use them while the replay reads its packets.
   only works for recordings made after this was added */
public final class ReplayComponentIds {
    public record Payload(List<Integer> ids, List<Identifier> names) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(GCUtils.modIdentifier("replay_component_ids"));
        public static final StreamCodec<FriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Payload::ids,
                Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), Payload::names,
                Payload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /* the recording's numbers, null = same as now */
    private static volatile Int2ObjectMap<Identifier> recorded;
    /* only while flashback's replay server reads recorded packets */
    private static final ThreadLocal<Boolean> READING = ThreadLocal.withInitial(() -> false);

    private ReplayComponentIds() {
    }

    /* the numbers right now, written into each snapshot while recording */
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

    /* true when the component number should be translated */
    public static boolean translating() {
        return recorded != null && READING.get();
    }

    /* a number from the recording -> the component it meant */
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
