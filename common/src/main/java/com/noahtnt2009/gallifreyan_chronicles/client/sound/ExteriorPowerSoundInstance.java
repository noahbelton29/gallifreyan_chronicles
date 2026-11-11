package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

/* the power on / power off sound, heard from outside the tardis.
   same distances and muffling as the outside hum (3 blocks shut, 6 open) */
public class ExteriorPowerSoundInstance extends ExteriorSoundInstance {
    private static final float OPEN_VOLUME = 1.0F;

    public ExteriorPowerSoundInstance(TardisExteriorBlockEntity exterior, SoundEvent sound) {
        super(exterior, sound, false);
        this.volume = computeVolume();
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().player == null || exteriorGone()) {
            this.volume = 0.0F;
            this.stop();
            return;
        }
        stepOpenness();
        this.volume = computeVolume();
        applyMuffle();
    }

    private float computeVolume() {
        if (Minecraft.getInstance().player == null) return 0.0F;
        return doorLoudness(OPEN_VOLUME) * falloff();
    }

    /* first tick happens after the sound starts, so set the muffle right away too */
    public void applyStartMuffle() {
        applyMuffle();
    }
}
