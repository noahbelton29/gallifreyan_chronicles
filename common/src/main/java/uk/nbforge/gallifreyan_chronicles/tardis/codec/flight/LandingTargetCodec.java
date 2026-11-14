package uk.nbforge.gallifreyan_chronicles.tardis.codec.flight;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;

public final class LandingTargetCodec {
    public static final Codec<LandingTarget> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(LandingTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(LandingTarget::pos),
            Codec.FLOAT.fieldOf("yaw").forGetter(LandingTarget::yaw)
    ).apply(inst, LandingTarget::new));

    private LandingTargetCodec() {
    }
}
