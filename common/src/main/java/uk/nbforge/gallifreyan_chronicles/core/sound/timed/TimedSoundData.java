package uk.nbforge.gallifreyan_chronicles.core.sound.timed;

import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
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
import uk.nbforge.gallifreyan_chronicles.codec.sound.TimedSoundDataCodec;

public class TimedSoundData extends SavedData {
    public record Session(ResourceKey<Level> dimension, Identifier sound, Vec3 pos, float volume, float pitch,
                          long startTick, int durationTicks, boolean loop, Optional<Identifier> next,
                          float nextVolume) {
    }

    public static final SavedDataType<TimedSoundData> TYPE = new SavedDataType<>(
            GCUtil.modIdentifier("timed_sounds"),
            TimedSoundData::new,
            TimedSoundDataCodec.CODEC,
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
