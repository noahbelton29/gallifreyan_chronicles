package uk.nbforge.gallifreyan_chronicles.codec.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSoundData;

public final class TimedSoundSessionCodec {
    public static final Codec<TimedSoundData.Session> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(TimedSoundData.Session::dimension),
            Identifier.CODEC.fieldOf("sound").forGetter(TimedSoundData.Session::sound),
            Vec3.CODEC.fieldOf("pos").forGetter(TimedSoundData.Session::pos),
            Codec.FLOAT.fieldOf("volume").forGetter(TimedSoundData.Session::volume),
            Codec.FLOAT.fieldOf("pitch").forGetter(TimedSoundData.Session::pitch),
            Codec.LONG.fieldOf("start_tick").forGetter(TimedSoundData.Session::startTick),
            Codec.INT.fieldOf("duration_ticks").forGetter(TimedSoundData.Session::durationTicks),
            Codec.BOOL.fieldOf("loop").forGetter(TimedSoundData.Session::loop),
            Identifier.CODEC.optionalFieldOf("next").forGetter(TimedSoundData.Session::next),
            Codec.FLOAT.optionalFieldOf("next_volume", 1.0F).forGetter(TimedSoundData.Session::nextVolume)
    ).apply(inst, TimedSoundData.Session::new));

    private TimedSoundSessionCodec() {
    }
}
