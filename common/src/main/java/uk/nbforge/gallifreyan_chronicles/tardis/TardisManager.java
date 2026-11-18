package uk.nbforge.gallifreyan_chronicles.tardis;

import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.manager.TardisManagerCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

/** Every TARDIS in the world, saved with the world. Each TARDIS's own data lives in its TardisData. */
public class TardisManager extends SavedData {
    public static final SavedDataType<TardisManager> TYPE = new SavedDataType<>(
            GCUtil.modIdentifier("tardises"),
            TardisManager::new,
            TardisManagerCodec.CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, TardisData> tardises = new HashMap<>();

    public static TardisManager get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public static TardisManager fromList(List<TardisData> list) {
        TardisManager manager = new TardisManager();
        list.forEach(manager::add);
        return manager;
    }

    private void add(TardisData data) {
        data.attach(this);
        tardises.put(data.id(), data);
    }

    /** The TARDIS with this id, null if there is none. */
    public @Nullable TardisData getTardis(@Nullable UUID tardisId) {
        return tardisId == null ? null : tardises.get(tardisId);
    }

    public TardisData createTardis(UUID ownerId, String ownerName) {
        TardisData data = new TardisData(new TardisComponent(UUID.randomUUID(), ownerId, ownerName));
        add(data);
        setDirty();
        return data;
    }

    public void removeTardis(UUID tardisId) {
        tardises.remove(tardisId);
        setDirty();
    }

    public Collection<TardisData> getAllTardises() {
        return tardises.values();
    }

    public Set<UUID> getAllIds() {
        return tardises.keySet();
    }

    public List<TardisData> getByOwner(UUID ownerId) {
        return tardises.values().stream()
                .filter(data -> data.component().ownerId().equals(ownerId))
                .toList();
    }

    /** Ids of every TARDIS that has an interior built. */
    public List<UUID> getInteriorIds() {
        return tardises.values().stream()
                .filter(data -> data.getInteriorOrigin() != null)
                .map(TardisData::id)
                .toList();
    }
}
