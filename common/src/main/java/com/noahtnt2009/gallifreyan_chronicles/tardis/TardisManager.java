package com.noahtnt2009.gallifreyan_chronicles.tardis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;

public class TardisManager extends SavedData {
    public static final Codec<TardisManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            TardisComponent.CODEC.listOf().fieldOf("tardises").forGetter(m -> new ArrayList<>(m.byTardisId.values()))
    ).apply(inst, components -> {
        TardisManager manager = new TardisManager();
        for (TardisComponent component : components) {
            manager.byTardisId.put(component.getTardisId(), component);
            manager.byOwnerId
                    .computeIfAbsent(component.getOwnerId(), ignored -> new ArrayList<>())
                    .add(component.getTardisId());
        }
        return manager;
    }));

    public static final SavedDataType<TardisManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "tardises"),
            TardisManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, TardisComponent> byTardisId = new HashMap<>();
    private final Map<UUID, List<UUID>> byOwnerId = new HashMap<>();

    public Optional<TardisComponent> get(UUID tardisId) {
        return Optional.ofNullable(byTardisId.get(tardisId));
    }

    public List<TardisComponent> getByOwner(UUID ownerId) {
        return byOwnerId.getOrDefault(ownerId, List.of())
                .stream()
                .map(byTardisId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public Set<UUID> getAllIds() {
        return byTardisId.keySet();
    }

    public TardisComponent create(UUID ownerId) {
        UUID tardisId = UUID.randomUUID();
        TardisComponent component = new TardisComponent(tardisId, ownerId);
        byTardisId.put(tardisId, component);
        byOwnerId.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(tardisId);
        setDirty();
        return component;
    }

    public void remove(UUID tardisId) {
        TardisComponent component = byTardisId.remove(tardisId);
        if (component != null) {
            List<UUID> owned = byOwnerId.get(component.getOwnerId());
            if (owned != null) owned.remove(tardisId);
        }
        setDirty();
    }

    public static TardisManager get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
}
