package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/* the interior hum, heard from outside the tardis.
   doors shut: only within 3 blocks, quieter and muffled.
   doors open: within 6 blocks and clear */
public class ExteriorHumSoundInstance extends AbstractTickableSoundInstance {
    public static final double OPEN_RANGE = 6.0;
    public static final double CLOSED_RANGE = 3.0;
    private static final float OPEN_VOLUME = 0.6F;
    /* same as the voice chat filter with the doors shut: gain 0.55 of open */
    private static final float CLOSED_VOLUME = OPEN_VOLUME * 0.55F;
    /* how much of the high sound gets through with the doors shut. the voice chat
       filter cuts everything above ~450hz, this is as close as openal's filter gets */
    public static final float CLOSED_MUFFLE = 0.02F;
    public static final float CHANGE_PER_TICK = 0.08F;
    /* power fades slower, about 1.5 seconds */
    private static final float POWER_FADE_PER_TICK = 0.035F;
    private static final float FADE_OUT_TICKS = 15.0F;

    private final TardisExteriorBlockEntity exterior;
    /* 0 = shut, 1 = open, slides between so opening the doors fades smoothly */
    private float openness;
    private float power;
    /* fading out after the player left this world (went through the door) */
    private boolean fadingOut;
    private float fadeOutStep;

    public ExteriorHumSoundInstance(TardisExteriorBlockEntity exterior) {
        super(GCSounds.FIRST_DOCTORS_HUM, SoundSource.BLOCKS, RandomSource.create());
        this.exterior = exterior;
        Vec3 center = Vec3.atCenterOf(exterior.getBlockPos());
        this.x = center.x;
        this.y = center.y;
        this.z = center.z;
        this.looping = true;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;
        this.openness = exterior.areDoorsFullyClosed() ? 0.0F : 1.0F;
        /* start silent and fade in with the power */
        this.power = 0.0F;
        this.volume = 0.0F;
    }

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            this.stop();
            return;
        }
        if (fadingOut || exterior.isRemoved() || exterior.getLevel() != player.level()) {
            /* went through the door (or the tardis left): fade out instead of cutting off */
            if (!fadingOut) {
                fadingOut = true;
                fadeOutStep = Math.max(0.01F, this.volume / FADE_OUT_TICKS);
            }
            this.volume = Math.max(0.0F, this.volume - fadeOutStep);
            if (this.volume <= 0.0F) this.stop();
            return;
        }

        openness = approach(openness, exterior.areDoorsFullyClosed() ? 0.0F : 1.0F, CHANGE_PER_TICK);
        power = approach(power, exterior.isPowered() ? 1.0F : 0.0F, POWER_FADE_PER_TICK);

        double range = CLOSED_RANGE + (OPEN_RANGE - CLOSED_RANGE) * openness;
        double distance = player.position().distanceTo(new Vec3(x, y, z));
        float falloff = distance >= range ? 0.0F : (float) (1.0 - distance / range);
        float loudness = CLOSED_VOLUME + (OPEN_VOLUME - CLOSED_VOLUME) * openness;
        this.volume = loudness * falloff * power * (float) GCConfig.tardisHumVolume();

        /* power off and fully faded: stop it. it starts again when the power comes back */
        if (power <= 0.0F && !exterior.isPowered()) {
            this.stop();
            return;
        }

        SoundMuffle.set(this, CLOSED_MUFFLE + (1.0F - CLOSED_MUFFLE) * openness);
    }

    /* stop it smoothly, see tick */
    public void fadeOut() {
        fadingOut = true;
        fadeOutStep = Math.max(0.01F, this.volume / FADE_OUT_TICKS);
    }

    private static float approach(float value, float target, float step) {
        if (value < target) return Math.min(target, value + step);
        if (value > target) return Math.max(target, value - step);
        return value;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
