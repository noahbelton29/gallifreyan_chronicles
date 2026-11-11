package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import net.minecraft.client.Minecraft;

/* the interior hum, heard from outside the tardis.
   doors shut: only within 3 blocks, quieter and muffled.
   doors open: within 6 blocks and clear */
public class ExteriorHumSoundInstance extends ExteriorSoundInstance {
    private static final float OPEN_VOLUME = 0.6F;
    private static final float POWER_FADE_PER_TICK = 0.035F;
    private static final float FADE_OUT_TICKS = 15.0F;

    private float power;
    private boolean fadingOut;
    private float fadeOutStep;

    public ExteriorHumSoundInstance(TardisExteriorBlockEntity exterior) {
        super(exterior, GCSounds.FIRST_DOCTORS_HUM, true);
        /* start silent and fade in with the power */
        this.power = 0.0F;
        this.volume = 0.0F;
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().player == null) {
            this.stop();
            return;
        }
        if (fadingOut || exteriorGone()) {
            /* went through the door (or the tardis left): fade out instead of cutting off */
            if (!fadingOut) fadeOut();
            this.volume = Math.max(0.0F, this.volume - fadeOutStep);
            if (this.volume <= 0.0F) this.stop();
            return;
        }

        stepOpenness();
        power = approach(power, exterior.isPowered() ? 1.0F : 0.0F, POWER_FADE_PER_TICK);
        this.volume = doorLoudness(OPEN_VOLUME) * falloff() * power * (float) GCConfig.tardisHumVolume();

        /* power off and fully faded: stop it. it starts again when the power comes back */
        if (power <= 0.0F && !exterior.isPowered()) {
            this.stop();
            return;
        }

        applyMuffle();
    }

    /* stop it smoothly, see tick */
    public void fadeOut() {
        fadingOut = true;
        fadeOutStep = Math.max(0.01F, this.volume / FADE_OUT_TICKS);
    }
}
