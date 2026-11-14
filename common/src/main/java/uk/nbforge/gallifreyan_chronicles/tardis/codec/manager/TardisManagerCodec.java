package uk.nbforge.gallifreyan_chronicles.tardis.codec.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.flight.LandingTargetCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.TardisMindCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.ecs.EcsComponentCodec;

public final class TardisManagerCodec {
    private static final Codec<TardisManager.Entry> ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EcsComponentCodec.TARDIS.fieldOf("tardis").forGetter(TardisManager.Entry::component),
            BlockPos.CODEC.optionalFieldOf("block_pos").forGetter(TardisManager.Entry::blockPos),
            BlockPos.CODEC.optionalFieldOf("console_block_pos").forGetter(TardisManager.Entry::consoleBlockPos),
            BlockPos.CODEC.optionalFieldOf("exterior_block_pos").forGetter(TardisManager.Entry::exteriorBlockPos),
            BlockPos.CODEC.optionalFieldOf("interior_origin").forGetter(TardisManager.Entry::interiorOrigin),
            Identifier.CODEC.optionalFieldOf("exterior_dimension").forGetter(TardisManager.Entry::exteriorDimension),
            Codec.STRING.optionalFieldOf("interior_id").forGetter(TardisManager.Entry::interiorId),
            LandingTargetCodec.CODEC.optionalFieldOf("landing_target").forGetter(TardisManager.Entry::landingTarget),
            Codec.INT.optionalFieldOf("coord_increment").forGetter(TardisManager.Entry::coordIncrement),
            Codec.BOOL.optionalFieldOf("coord_increment_negative").forGetter(TardisManager.Entry::coordIncrementNegative),
            EcsComponentCodec.UUID_CODEC.listOf().optionalFieldOf("trusted").forGetter(TardisManager.Entry::trusted),
            TardisMindCodec.CODEC.optionalFieldOf("mind").forGetter(TardisManager.Entry::mind),
            Codec.LONG.optionalFieldOf("flight_start").forGetter(TardisManager.Entry::flightStart),
            Codec.BOOL.optionalFieldOf("crashed").forGetter(TardisManager.Entry::crashed)
    ).apply(inst, TardisManager.Entry::new));

    public static final Codec<TardisManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ENTRY_CODEC.listOf().fieldOf("tardises").forGetter(TardisManager::toEntries)
    ).apply(inst, TardisManager::fromEntries));

    private TardisManagerCodec() {
    }
}
