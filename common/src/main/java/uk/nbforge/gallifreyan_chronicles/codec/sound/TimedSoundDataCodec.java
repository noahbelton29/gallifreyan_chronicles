package uk.nbforge.gallifreyan_chronicles.codec.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSoundData;

public final class TimedSoundDataCodec {
    public static final Codec<TimedSoundData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            TimedSoundSessionCodec.CODEC.listOf().fieldOf("sessions").forGetter(TimedSoundData::sessions)
    ).apply(inst, sessions -> {
        TimedSoundData data = new TimedSoundData();
        data.sessions().addAll(sessions);
        return data;
    }));

    private TimedSoundDataCodec() {
    }
}
