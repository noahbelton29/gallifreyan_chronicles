package uk.nbforge.gallifreyan_chronicles.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;

public class ExteriorPowerSoundInstance extends ExteriorSoundInstance {
    private static final float OPEN_VOLUME = 1.0f;

    public ExteriorPowerSoundInstance(TardisExteriorBlockEntity exterior, SoundEvent sound) {
        super(exterior, sound, false);
        this.volume = computeVolume();
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().player == null || exteriorGone()) {
            this.volume = 0.0f;
            this.stop();
            return;
        }
        stepOpenness();
        this.volume = computeVolume();
        applyMuffle();
    }

    private float computeVolume() {
        if (Minecraft.getInstance().player == null) return 0.0f;
        return doorLoudness(OPEN_VOLUME) * falloff();
    }

    /** The first tick happens after the sound starts so set the muffle right away too. */
    public void applyStartMuffle() {
        applyMuffle();
    }
}
