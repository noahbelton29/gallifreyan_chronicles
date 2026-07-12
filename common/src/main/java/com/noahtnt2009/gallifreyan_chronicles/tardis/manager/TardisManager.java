package com.noahtnt2009.gallifreyan_chronicles.tardis.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class TardisManager extends SavedData {
    private record Entry(TardisComponent component, BlockPos blockPos, BlockPos consoleBlockPos, BlockPos exteriorBlockPos) {
    }

    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            TardisComponent.CODEC.fieldOf("tardis").forGetter(Entry::component),
            BlockPos.CODEC.optionalFieldOf("block_pos", null).forGetter(Entry::blockPos),
            BlockPos.CODEC.optionalFieldOf("console_block_pos", null).forGetter(Entry::consoleBlockPos),
            BlockPos.CODEC.optionalFieldOf("exterior_block_pos", null).forGetter(Entry::exteriorBlockPos)
    ).apply(inst, Entry::new));

    public static final Codec<TardisManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ENTRY_CODEC.listOf().fieldOf("tardises").forGetter(m -> m.byTardisId.values().stream()
                    .map(c -> new Entry(
                            c,
                            m.blockPosById.get(c.getTardisId()),
                            m.consoleBlockPosById.get(c.getTardisId()),
                            m.exteriorBlockPosById.get(c.getTardisId())
                    ))
                    .toList())
    ).apply(inst, entries -> {
        TardisManager manager = new TardisManager();
        for (Entry entry : entries) {
            TardisComponent component = entry.component();
            manager.byTardisId.put(component.getTardisId(), component);
            manager.byOwnerId
                    .computeIfAbsent(component.getOwnerId(), ignored -> new ArrayList<>())
                    .add(component.getTardisId());
            if (entry.blockPos() != null) {
                manager.blockPosById.put(component.getTardisId(), entry.blockPos());
            }
            if (entry.consoleBlockPos() != null) {
                manager.consoleBlockPosById.put(component.getTardisId(), entry.consoleBlockPos());
            }
            if (entry.exteriorBlockPos() != null) {
                manager.exteriorBlockPosById.put(component.getTardisId(), entry.exteriorBlockPos());
            }
        }
        return manager;
    }));

    public static final SavedDataType<TardisManager> TYPE = new SavedDataType<>(
            GCUtils.of("tardises"),
            TardisManager::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, TardisComponent> byTardisId = new HashMap<>();
    private final Map<UUID, List<UUID>> byOwnerId = new HashMap<>();
    private final Map<UUID, BlockPos> blockPosById = new HashMap<>();
    private final Map<UUID, BlockPos> consoleBlockPosById = new HashMap<>();
    private final Map<UUID, BlockPos> exteriorBlockPosById = new HashMap<>();

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
        blockPosById.remove(tardisId);
        consoleBlockPosById.remove(tardisId);
        exteriorBlockPosById.remove(tardisId);
        setDirty();
    }

    public void setBlockPos(UUID tardisId, BlockPos pos) {
        if (pos == null) {
            blockPosById.remove(tardisId);
        } else {
            blockPosById.put(tardisId, pos);
        }
        setDirty();
    }

    public @Nullable BlockPos getBlockPos(UUID tardisId) {
        return blockPosById.get(tardisId);
    }

    public void setConsoleBlockPos(UUID tardisId, BlockPos pos) {
        if (pos == null) {
            consoleBlockPosById.remove(tardisId);
        } else {
            consoleBlockPosById.put(tardisId, pos);
        }
        setDirty();
    }

    public @Nullable BlockPos getConsoleBlockPos(UUID tardisId) {
        return consoleBlockPosById.get(tardisId);
    }

    public void unlinkConsole(UUID tardisId) {
        setConsoleBlockPos(tardisId, null);
    }

    public void setExteriorBlockPos(UUID tardisId, BlockPos pos) {
        if (pos == null) {
            exteriorBlockPosById.remove(tardisId);
        } else {
            exteriorBlockPosById.put(tardisId, pos);
        }
        setDirty();
    }

    public @Nullable BlockPos getExteriorBlockPos(UUID tardisId) {
        return exteriorBlockPosById.get(tardisId);
    }

    public void unlinkExterior(UUID tardisId) {
        setExteriorBlockPos(tardisId, null);
    }

    public @Nullable BlockPos resolveExteriorBlockPos(UUID tardisId) {
        BlockPos linked = exteriorBlockPosById.get(tardisId);
        return linked != null ? linked : blockPosById.get(tardisId);
    }

    public static TardisManager get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
}
