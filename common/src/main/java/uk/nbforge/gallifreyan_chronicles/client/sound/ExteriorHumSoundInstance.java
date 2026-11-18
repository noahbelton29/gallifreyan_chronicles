package uk.nbforge.gallifreyan_chronicles.client.sound;

import net.minecraft.client.Minecraft;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;

public class ExteriorHumSoundInstance extends ExteriorSoundInstance {
    private static final float OPEN_VOLUME = 0.6f;
    private static final float POWER_FADE_PER_TICK = 0.035f;
    private static final float FADE_OUT_TICKS = 15.0f;

    private float power;
    private boolean fadingOut;
    private float fadeOutStep;

    public ExteriorHumSoundInstance(TardisExteriorBlockEntity exterior) {
        super(exterior, GCSounds.FIRST_DOCTORS_HUM, true);
        this.power = exterior.lamp().isPowered() ? 1.0f : 0.0f;
        this.volume = 0.0f;
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().player == null) {
            this.stop();
            return;
        }
        if (fadingOut || exteriorGone()) {
            if (!fadingOut) fadeOut();
            this.volume = Math.max(0.0f, this.volume - fadeOutStep);
            if (this.volume <= 0.0f) this.stop();
            return;
        }

        stepOpenness();
        power = approach(power, exterior.lamp().isPowered() ? 1.0f : 0.0f, POWER_FADE_PER_TICK);
        this.volume = doorLoudness(OPEN_VOLUME) * falloff() * power * (float) GCConfig.tardisHumVolume();

        if (power <= 0.0f && !exterior.lamp().isPowered()) {
            this.stop();
            return;
        }

        applyMuffle();
    }

    public void fadeOut() {
        fadingOut = true;
        fadeOutStep = Math.max(0.01f, this.volume / FADE_OUT_TICKS);
    }
}
