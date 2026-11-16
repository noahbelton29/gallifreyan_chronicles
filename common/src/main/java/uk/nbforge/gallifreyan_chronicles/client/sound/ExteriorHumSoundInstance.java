package uk.nbforge.gallifreyan_chronicles.client.sound;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import net.minecraft.client.Minecraft;

/**
   The interior hum heard from outside the TARDIS.
   With the doors shut only within 3 blocks and quieter and muffled, with them open within 6 blocks and clear.
*/
public class ExteriorHumSoundInstance extends ExteriorSoundInstance {
    private static final float OPEN_VOLUME = 0.6F;
    private static final float POWER_FADE_PER_TICK = 0.035F;
    private static final float FADE_OUT_TICKS = 15.0F;

    private float power;
    private boolean fadingOut;
    private float fadeOutStep;

    public ExteriorHumSoundInstance(TardisExteriorBlockEntity exterior) {
        super(exterior, GCSounds.FIRST_DOCTORS_HUM, true);
        // Start at the current power level right away, walking out through the door or coming back
        // into range should sound the same instantly, the fade is only for an actual power change
        // (approach() in tick() still ramps this if isPowered() flips while this instance is alive)
        this.power = exterior.isPowered() ? 1.0F : 0.0F;
        this.volume = 0.0F;
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().player == null) {
            this.stop();
            return;
        }
        if (fadingOut || exteriorGone()) {
            // Went through the door or the TARDIS left, fade out instead of cutting off
            if (!fadingOut) fadeOut();
            this.volume = Math.max(0.0F, this.volume - fadeOutStep);
            if (this.volume <= 0.0F) this.stop();
            return;
        }

        stepOpenness();
        power = approach(power, exterior.isPowered() ? 1.0F : 0.0F, POWER_FADE_PER_TICK);
        this.volume = doorLoudness(OPEN_VOLUME) * falloff() * power * (float) GCConfig.tardisHumVolume();

        // Power off and fully faded so stop it, it starts again when the power comes back
        if (power <= 0.0F && !exterior.isPowered()) {
            this.stop();
            return;
        }

        applyMuffle();
    }

    /** Stops it smoothly, see tick. */
    public void fadeOut() {
        fadingOut = true;
        fadeOutStep = Math.max(0.01F, this.volume / FADE_OUT_TICKS);
    }
}
