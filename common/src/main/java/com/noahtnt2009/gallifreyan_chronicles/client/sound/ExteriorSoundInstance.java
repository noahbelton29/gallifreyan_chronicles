package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/* a sound played at the exterior that gets louder, further reaching and clearer as the doors open */
public abstract class ExteriorSoundInstance extends AbstractTickableSoundInstance {
    public static final double OPEN_RANGE = 6.0;
    public static final double CLOSED_RANGE = 3.0;
    public static final float CLOSED_MUFFLE = 0.02F;
    public static final float CHANGE_PER_TICK = 0.08F;
    protected static final float CLOSED_GAIN = 0.55F;

    protected final TardisExteriorBlockEntity exterior;
    protected float openness;

    protected ExteriorSoundInstance(TardisExteriorBlockEntity exterior, SoundEvent sound, boolean looping) {
        super(sound, SoundSource.BLOCKS, RandomSource.create());
        this.exterior = exterior;
        Vec3 center = Vec3.atCenterOf(exterior.getBlockPos());
        this.x = center.x;
        this.y = center.y;
        this.z = center.z;
        this.looping = looping;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;
        this.openness = doorsOpenTarget();
    }

    protected boolean exteriorGone() {
        return exterior.isRemoved() || exterior.getLevel() != SoundListener.level();
    }

    protected float doorsOpenTarget() {
        return exterior.areDoorsFullyClosed() ? 0.0F : 1.0F;
    }

    protected void stepOpenness() {
        openness = approach(openness, doorsOpenTarget(), CHANGE_PER_TICK);
    }

    protected float falloff() {
        double range = CLOSED_RANGE + (OPEN_RANGE - CLOSED_RANGE) * openness;
        double distance = SoundListener.pos().distanceTo(new Vec3(x, y, z));
        return distance >= range ? 0.0F : (float) (1.0 - distance / range);
    }

    protected float doorLoudness(float openVolume) {
        float closedVolume = openVolume * CLOSED_GAIN;
        return closedVolume + (openVolume - closedVolume) * openness;
    }

    protected void applyMuffle() {
        SoundMuffle.set(this, CLOSED_MUFFLE + (1.0F - CLOSED_MUFFLE) * openness);
    }

    protected static float approach(float value, float target, float step) {
        if (value < target) return Math.min(target, value + step);
        if (value > target) return Math.max(target, value - step);
        return value;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
