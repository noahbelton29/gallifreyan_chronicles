package uk.nbforge.gallifreyan_chronicles.tardis.codec.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.ecs.EcsComponentCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.flight.LandingTargetCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.TardisMindCodec;

/** Field names match the old save format, so existing worlds load unchanged. Defaults are left out when saving. */
public final class TardisManagerCodec {
    private static final Codec<TardisData> TARDIS_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    EcsComponentCodec.TARDIS.fieldOf("tardis").forGetter(TardisData::component),
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
                    LandingTargetCodec.CODEC
                            .optionalFieldOf("landing_target")
                            .forGetter(t -> Optional.ofNullable(t.getLandingTarget())),
                    Codec.INT.optionalFieldOf("coord_increment", 0).forGetter(TardisData::getCoordIncrementIndex),
                    Codec.BOOL
                            .optionalFieldOf("coord_increment_negative", false)
                            .forGetter(TardisData::isCoordIncrementNegative),
                    EcsComponentCodec.UUID_CODEC
                            .listOf()
                            .optionalFieldOf("trusted", List.of())
                            .forGetter(t -> List.copyOf(t.trustedPlayers())),
                    TardisMindCodec.CODEC.optionalFieldOf("mind").forGetter(t -> Optional.ofNullable(t.savedMind())),
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
                        t.setCoordIncrementIndex(coordIncrement);
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

    private TardisManagerCodec() {}
}
