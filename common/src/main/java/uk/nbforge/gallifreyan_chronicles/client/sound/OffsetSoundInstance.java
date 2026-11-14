package uk.nbforge.gallifreyan_chronicles.client.sound;

import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class OffsetSoundInstance extends SimpleSoundInstance {
    private final float offsetSeconds;

    public OffsetSoundInstance(SoundEvent sound, float volume, float pitch, double x, double y, double z, float offsetSeconds) {
        super(sound, SoundSource.BLOCKS, volume, pitch, SoundInstance.createUnseededRandom(), x, y, z);
        this.offsetSeconds = offsetSeconds;
    }

    public float offsetSeconds() {
        return offsetSeconds;
    }
}
