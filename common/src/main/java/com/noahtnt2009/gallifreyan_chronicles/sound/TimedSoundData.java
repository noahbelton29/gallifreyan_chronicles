package com.noahtnt2009.gallifreyan_chronicles.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimedSoundData extends SavedData {
    public record Session(ResourceKey<Level> dimension, Identifier sound, Vec3 pos, float volume, float pitch,
                          long startTick, int durationTicks, boolean loop, Optional<Identifier> next,
                          float nextVolume) {
        public static final Codec<Session> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(Session::dimension),
                Identifier.CODEC.fieldOf("sound").forGetter(Session::sound),
                Vec3.CODEC.fieldOf("pos").forGetter(Session::pos),
                Codec.FLOAT.fieldOf("volume").forGetter(Session::volume),
                Codec.FLOAT.fieldOf("pitch").forGetter(Session::pitch),
                Codec.LONG.fieldOf("start_tick").forGetter(Session::startTick),
                Codec.INT.fieldOf("duration_ticks").forGetter(Session::durationTicks),
                Codec.BOOL.fieldOf("loop").forGetter(Session::loop),
                Identifier.CODEC.optionalFieldOf("next").forGetter(Session::next),
                Codec.FLOAT.optionalFieldOf("next_volume", 1.0F).forGetter(Session::nextVolume)
        ).apply(inst, Session::new));
    }

    public static final Codec<TimedSoundData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Session.CODEC.listOf().fieldOf("sessions").forGetter(data -> data.sessions)
    ).apply(inst, sessions -> {
        TimedSoundData data = new TimedSoundData();
        data.sessions.addAll(sessions);
        return data;
    }));

    public static final SavedDataType<TimedSoundData> TYPE = new SavedDataType<>(
            GCUtils.modIdentifier("timed_sounds"),
            TimedSoundData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final List<Session> sessions = new ArrayList<>();

    public static TimedSoundData get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public List<Session> sessions() {
        return sessions;
    }

    public void add(Session session) {
        sessions.add(session);
        setDirty();
    }

    public boolean remove(Session session) {
        boolean removed = sessions.remove(session);
        if (removed) setDirty();
        return removed;
    }

    public void replace(Session old, Session replacement) {
        int index = sessions.indexOf(old);
        if (index < 0) return;
        sessions.set(index, replacement);
        setDirty();
    }
}
