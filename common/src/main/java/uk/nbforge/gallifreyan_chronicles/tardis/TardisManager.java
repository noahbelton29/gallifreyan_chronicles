package uk.nbforge.gallifreyan_chronicles.tardis;

import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import uk.nbforge.gallifreyan_chronicles.tardis.codec.manager.TardisManagerCodec;
import java.util.*;

public class TardisManager extends SavedData {
    public record Entry(TardisComponent component, Optional<BlockPos> blockPos,
                        Optional<BlockPos> consoleBlockPos, Optional<BlockPos> exteriorBlockPos,
                        Optional<BlockPos> interiorOrigin, Optional<Identifier> exteriorDimension,
                        Optional<String> interiorId, Optional<LandingTarget> landingTarget,
                        Optional<Integer> coordIncrement, Optional<Boolean> coordIncrementNegative,
                        Optional<List<UUID>> trusted, Optional<TardisMind> mind,
                        Optional<Long> flightStart, Optional<Boolean> crashed) {
    }

    public static final SavedDataType<TardisManager> TYPE = new SavedDataType<>(
            GCUtil.modIdentifier("tardises"),
            TardisManager::new,
            TardisManagerCodec.CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, TardisComponent> byTardisId = new HashMap<>();
    private final Map<UUID, List<UUID>> byOwnerId = new HashMap<>();
    private final Map<UUID, BlockPos> blockPosById = new HashMap<>();
    private final Map<UUID, BlockPos> consoleBlockPosById = new HashMap<>();
    private final Map<UUID, BlockPos> exteriorBlockPosById = new HashMap<>();
    private final Map<UUID, BlockPos> interiorOriginById = new HashMap<>();
    private final Map<UUID, Identifier> exteriorDimensionById = new HashMap<>();
    private final Map<UUID, String> interiorIdById = new HashMap<>();
    private final Map<UUID, LandingTarget> landingTargetById = new HashMap<>();
    private final Map<UUID, Integer> coordIncrementById = new HashMap<>();
    private final Set<UUID> negativeIncrements = new HashSet<>();
    private final Map<UUID, Set<UUID>> trustedById = new HashMap<>();
    private final Map<UUID, TardisMind> mindById = new HashMap<>();
    private final Map<UUID, Long> flightStartById = new HashMap<>();
    private final Set<UUID> crashed = new HashSet<>();

    public List<Entry> toEntries() {
        List<Entry> entries = new ArrayList<>();
        for (TardisComponent component : byTardisId.values()) {
            UUID id = component.tardisId();
            entries.add(new Entry(
                    component,
                    Optional.ofNullable(blockPosById.get(id)),
                    Optional.ofNullable(consoleBlockPosById.get(id)),
                    Optional.ofNullable(exteriorBlockPosById.get(id)),
                    Optional.ofNullable(interiorOriginById.get(id)),
                    Optional.ofNullable(exteriorDimensionById.get(id)),
                    Optional.ofNullable(interiorIdById.get(id)),
                    Optional.ofNullable(landingTargetById.get(id)),
                    Optional.ofNullable(coordIncrementById.get(id)),
                    negativeIncrements.contains(id) ? Optional.of(true) : Optional.empty(),
                    trustedById.containsKey(id)
                            ? Optional.of(List.copyOf(trustedById.get(id)))
                            : Optional.empty(),
                    Optional.ofNullable(mindById.get(id)),
                    Optional.ofNullable(flightStartById.get(id)),
                    crashed.contains(id) ? Optional.of(true) : Optional.empty()
            ));
        }
        return entries;
    }

    public static TardisManager fromEntries(List<Entry> entries) {
        TardisManager manager = new TardisManager();
        for (Entry entry : entries) {
            TardisComponent component = entry.component();
            UUID id = component.tardisId();

            manager.byTardisId.put(id, component);
            manager.byOwnerId
                    .computeIfAbsent(component.ownerId(), ignored -> new ArrayList<>())
                    .add(id);

            entry.blockPos().ifPresent(pos -> manager.blockPosById.put(id, pos));
            entry.consoleBlockPos().ifPresent(pos -> manager.consoleBlockPosById.put(id, pos));
            entry.exteriorBlockPos().ifPresent(pos -> manager.exteriorBlockPosById.put(id, pos));
            entry.interiorOrigin().ifPresent(pos -> manager.interiorOriginById.put(id, pos));
            entry.exteriorDimension().ifPresent(dim -> manager.exteriorDimensionById.put(id, dim));
            entry.interiorId().ifPresent(interior -> manager.interiorIdById.put(id, interior));
            entry.landingTarget().ifPresent(target -> manager.landingTargetById.put(id, target));
            entry.coordIncrement().ifPresent(index -> manager.coordIncrementById.put(id, index));
            if (entry.coordIncrementNegative().orElse(false)) manager.negativeIncrements.add(id);
            entry.trusted().ifPresent(players -> manager.trustedById.put(id, new HashSet<>(players)));
            entry.mind().ifPresent(mind -> manager.mindById.put(id, mind));
            entry.flightStart().ifPresent(start -> manager.flightStartById.put(id, start));
            if (entry.crashed().orElse(false)) manager.crashed.add(id);
        }
        return manager;
    }

    public Optional<TardisComponent> get(UUID tardisId) {
        if (tardisId == null) {
            return Optional.empty();
        }
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

    public TardisComponent create(UUID ownerId, String ownerName) {
        UUID tardisId = UUID.randomUUID();
        TardisComponent component = new TardisComponent(tardisId, ownerId, ownerName);
        byTardisId.put(tardisId, component);
        byOwnerId.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(tardisId);
        setDirty();
        return component;
    }

    public void setOwner(UUID tardisId, UUID ownerId, String ownerName) {
        TardisComponent old = byTardisId.get(tardisId);
        if (old == null) return;
        List<UUID> previous = byOwnerId.get(old.ownerId());
        if (previous != null) previous.remove(tardisId);
        byTardisId.put(tardisId, new TardisComponent(tardisId, ownerId, ownerName));
        byOwnerId.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(tardisId);
        setDirty();
    }

    public void updateOwnerName(UUID tardisId, String ownerName) {
        TardisComponent old = byTardisId.get(tardisId);
        if (old == null || old.ownerName().equals(ownerName)) return;
        byTardisId.put(tardisId, new TardisComponent(old.tardisId(), old.ownerId(), ownerName));
        setDirty();
    }

    public boolean isTrusted(UUID tardisId, UUID playerId) {
        return trustedById.getOrDefault(tardisId, Set.of()).contains(playerId);
    }

    public Set<UUID> trustedOf(UUID tardisId) {
        return Set.copyOf(trustedById.getOrDefault(tardisId, Set.of()));
    }

    public boolean setTrusted(UUID tardisId, UUID playerId, boolean trusted) {
        if (!byTardisId.containsKey(tardisId)) return false;
        Set<UUID> players = trustedById.computeIfAbsent(tardisId, ignored -> new HashSet<>());
        boolean changed = trusted ? players.add(playerId) : players.remove(playerId);
        if (players.isEmpty()) trustedById.remove(tardisId);
        if (changed) setDirty();
        return changed;
    }

    public void remove(UUID tardisId) {
        TardisComponent component = byTardisId.remove(tardisId);
        if (component != null) {
            List<UUID> owned = byOwnerId.get(component.ownerId());
            if (owned != null) owned.remove(tardisId);
        }
        blockPosById.remove(tardisId);
        consoleBlockPosById.remove(tardisId);
        exteriorBlockPosById.remove(tardisId);
        interiorOriginById.remove(tardisId);
        exteriorDimensionById.remove(tardisId);
        interiorIdById.remove(tardisId);
        landingTargetById.remove(tardisId);
        coordIncrementById.remove(tardisId);
        negativeIncrements.remove(tardisId);
        trustedById.remove(tardisId);
        mindById.remove(tardisId);
        flightStartById.remove(tardisId);
        crashed.remove(tardisId);
        setDirty();
    }

    public boolean isCrashed(UUID tardisId) {
        return crashed.contains(tardisId);
    }

    public void setCrashed(UUID tardisId, boolean isCrashed) {
        boolean changed = isCrashed ? crashed.add(tardisId) : crashed.remove(tardisId);
        if (changed) setDirty();
    }

    public @Nullable Long getFlightStart(UUID tardisId) {
        return flightStartById.get(tardisId);
    }

    public void setFlightStart(UUID tardisId, @Nullable Long start) {
        if (start == null) flightStartById.remove(tardisId);
        else flightStartById.put(tardisId, start);
        setDirty();
    }

    public TardisMind mind(UUID tardisId) {
        TardisMind mind = mindById.get(tardisId);
        if (mind != null) return mind;
        mind = TardisMind.roll(tardisId);
        mindById.put(tardisId, mind);
        setDirty();
        return mind;
    }

    public void setMind(UUID tardisId, TardisMind mind) {
        mindById.put(tardisId, mind);
        setDirty();
    }

    // where the tardis was first placed. used when no exterior is linked
    public void setRegisteredBlockPos(UUID tardisId, BlockPos pos) {
        if (pos == null) {
            blockPosById.remove(tardisId);
        } else {
            blockPosById.put(tardisId, pos);
        }
        setDirty();
    }

    public @Nullable BlockPos getRegisteredBlockPos(UUID tardisId) {
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

    public void setLandingTarget(UUID tardisId, @Nullable LandingTarget target) {
        if (target == null) {
            landingTargetById.remove(tardisId);
        } else {
            landingTargetById.put(tardisId, target);
        }
        setDirty();
    }

    // index into TardisDestinations.INCREMENTS, the step the console's x/y/z controls move the destination by
    public int getCoordIncrementIndex(UUID tardisId) {
        return coordIncrementById.getOrDefault(tardisId, 0);
    }

    // when set, the x/y/z controls subtract the increment instead of adding it
    public boolean isCoordIncrementNegative(UUID tardisId) {
        return negativeIncrements.contains(tardisId);
    }

    public void setCoordIncrementNegative(UUID tardisId, boolean negative) {
        if (negative) negativeIncrements.add(tardisId);
        else negativeIncrements.remove(tardisId);
        setDirty();
    }

    public void setCoordIncrementIndex(UUID tardisId, int index) {
        coordIncrementById.put(tardisId, index);
        setDirty();
    }

    public @Nullable LandingTarget getLandingTarget(UUID tardisId) {
        return landingTargetById.get(tardisId);
    }

    public @Nullable String getInteriorId(UUID tardisId) {
        return interiorIdById.get(tardisId);
    }

    public void setInteriorId(UUID tardisId, String interiorId) {
        interiorIdById.put(tardisId, interiorId);
        setDirty();
    }

    public @Nullable BlockPos getInteriorOrigin(UUID tardisId) {
        return interiorOriginById.get(tardisId);
    }

    public void clearInteriorOrigin(UUID tardisId) {
        interiorOriginById.remove(tardisId);
        setDirty();
    }

    public void setInteriorOrigin(UUID tardisId, BlockPos origin) {
        interiorOriginById.put(tardisId, origin);
        setDirty();
    }

    public void setExteriorDimension(UUID tardisId, ResourceKey<Level> dimension) {
        exteriorDimensionById.put(tardisId, dimension.identifier());
        setDirty();
    }

    public ResourceKey<Level> getExteriorDimension(UUID tardisId) {
        Identifier id = exteriorDimensionById.get(tardisId);
        return id == null ? Level.OVERWORLD : ResourceKey.create(Registries.DIMENSION, id);
    }

    public List<UUID> getInteriorIds() {
        return new ArrayList<>(interiorOriginById.keySet());
    }

    public static TardisManager get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
}