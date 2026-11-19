package uk.nbforge.gallifreyan_chronicles.tardis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.codec.UuidCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class TardisManager extends SavedData {
    /** field names match the old save format, dont rename them or every existing world is fucked */
    private static final Codec<TardisData> TARDIS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    TardisIdentity.CODEC.fieldOf("tardis").forGetter(TardisData::component),
                    BlockPos.CODEC
                            .optionalFieldOf("block_pos")
                            .forGetter(t -> Optional.ofNullable(t.getRegisteredBlockPos())),
                    BlockPos.CODEC
                            .optionalFieldOf("console_block_pos")
                            .forGetter(t -> Optional.ofNullable(t.getConsoleBlockPos())),
                    BlockPos.CODEC
                            .optionalFieldOf("exterior_block_pos")
                            .forGetter(t -> Optional.ofNullable(t.getExteriorBlockPos())),
                    BlockPos.CODEC
                            .optionalFieldOf("interior_origin")
                            .forGetter(t -> Optional.ofNullable(t.getInteriorOrigin())),
                    Identifier.CODEC
                            .optionalFieldOf("exterior_dimension")
                            .forGetter(t -> Optional.ofNullable(t.getExteriorDimensionId())),
                    Codec.STRING.optionalFieldOf("interior_id").forGetter(t -> Optional.ofNullable(t.getInteriorId())),
                    LandingTarget.CODEC
                            .optionalFieldOf("landing_target")
                            .forGetter(t -> Optional.ofNullable(t.getLandingTarget())),
                    LandingTarget.CODEC
                            .optionalFieldOf("pending_target")
                            .forGetter(t -> Optional.ofNullable(t.getPendingLandingTarget())),
                    Codec.INT.optionalFieldOf("coord_increment_amount", 1).forGetter(TardisData::getCoordIncrement),
                    Codec.BOOL
                            .optionalFieldOf("coord_increment_negative", false)
                            .forGetter(TardisData::isCoordIncrementNegative),
                    UuidCodec.CODEC
                            .listOf()
                            .optionalFieldOf("trusted", List.of())
                            .forGetter(t -> List.copyOf(t.trustedPlayers())),
                    TardisMind.CODEC.optionalFieldOf("mind").forGetter(t -> Optional.ofNullable(t.savedMind())),
                    Codec.LONG.optionalFieldOf("flight_start").forGetter(t -> Optional.ofNullable(t.getFlightStart())),
                    Codec.BOOL.optionalFieldOf("crashed", false).forGetter(TardisData::isCrashed),
                    Codec.LONG
                            .optionalFieldOf("flight_duration_override")
                            .forGetter(t -> Optional.ofNullable(t.getFlightDurationOverride())))
            .apply(
                    inst,
                    (component,
                            blockPos,
                            consoleBlockPos,
                            exteriorBlockPos,
                            interiorOrigin,
                            exteriorDimension,
                            interiorId,
                            landingTarget,
                            pendingTarget,
                            coordIncrement,
                            coordIncrementNegative,
                            trusted,
                            mind,
                            flightStart,
                            crashed,
                            flightDurationOverride) -> {
                        TardisData t = new TardisData(component);
                        t.setRegisteredBlockPos(blockPos.orElse(null));
                        t.setConsoleBlockPos(consoleBlockPos.orElse(null));
                        t.setExteriorBlockPos(exteriorBlockPos.orElse(null));
                        t.setInteriorOrigin(interiorOrigin.orElse(null));
                        t.setExteriorDimension(exteriorDimension
                                .map(id -> ResourceKey.create(Registries.DIMENSION, id))
                                .orElse(null));
                        t.setInteriorId(interiorId.orElse(null));
                        t.setLandingTarget(landingTarget.orElse(null));
                        t.setPendingLandingTarget(pendingTarget.orElse(null));
                        t.setCoordIncrement(coordIncrement);
                        t.setCoordIncrementNegative(coordIncrementNegative);
                        trusted.forEach(player -> t.setTrusted(player, true));
                        mind.ifPresent(t::setMind);
                        t.setFlightStart(flightStart.orElse(null));
                        t.setCrashed(crashed);
                        t.setFlightDurationOverride(flightDurationOverride.orElse(null));
                        return t;
                    }));

    public static final Codec<TardisManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(TARDIS_CODEC
                    .listOf()
                    .fieldOf("tardises")
                    .forGetter(manager -> List.copyOf(manager.getAllTardises())))
            .apply(inst, TardisManager::fromList));

    public static final SavedDataType<TardisManager> TYPE = new SavedDataType<>(
            GCUtil.modIdentifier("tardises"), TardisManager::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

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

    @Nullable
    TardisData getTardis(@Nullable UUID tardisId) {
        return tardisId == null ? null : tardises.get(tardisId);
    }

    public TardisData createTardis(UUID ownerId, String ownerName) {
        TardisData data = new TardisData(new TardisIdentity(UUID.randomUUID(), ownerId, ownerName));
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

    public List<UUID> getInteriorIds() {
        return tardises.values().stream()
                .filter(data -> data.getInteriorOrigin() != null)
                .map(TardisData::id)
                .toList();
    }
}
