package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/* the power on / power off sound, heard from outside the tardis.
   same distances and muffling as the outside hum (3 blocks shut, 6 open) */
public class ExteriorPowerSoundInstance extends AbstractTickableSoundInstance {
    private static final float OPEN_VOLUME = 1.0F;
    private static final float CLOSED_VOLUME = OPEN_VOLUME * 0.55F;

    private final TardisExteriorBlockEntity exterior;
    private float openness;

    public ExteriorPowerSoundInstance(TardisExteriorBlockEntity exterior, SoundEvent sound) {
        super(sound, SoundSource.BLOCKS, RandomSource.create());
        this.exterior = exterior;
        Vec3 center = Vec3.atCenterOf(exterior.getBlockPos());
        this.x = center.x;
        this.y = center.y;
        this.z = center.z;
        this.looping = false;
        this.delay = 0;
        this.attenuation = Attenuation.NONE;
        this.openness = exterior.areDoorsFullyClosed() ? 0.0F : 1.0F;
        this.volume = computeVolume();
    }

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || exterior.isRemoved() || exterior.getLevel() != SoundListener.level()) {
            this.volume = 0.0F;
            this.stop();
            return;
        }
        float target = exterior.areDoorsFullyClosed() ? 0.0F : 1.0F;
        if (openness < target) openness = Math.min(target, openness + ExteriorHumSoundInstance.CHANGE_PER_TICK);
        else if (openness > target) openness = Math.max(target, openness - ExteriorHumSoundInstance.CHANGE_PER_TICK);
        this.volume = computeVolume();
        float muffle = ExteriorHumSoundInstance.CLOSED_MUFFLE;
        SoundMuffle.set(this, muffle + (1.0F - muffle) * openness);
    }

    private float computeVolume() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0.0F;
        double range = ExteriorHumSoundInstance.CLOSED_RANGE
                + (ExteriorHumSoundInstance.OPEN_RANGE - ExteriorHumSoundInstance.CLOSED_RANGE) * openness;
        double distance = SoundListener.pos().distanceTo(new Vec3(x, y, z));
        float falloff = distance >= range ? 0.0F : (float) (1.0 - distance / range);
        return (CLOSED_VOLUME + (OPEN_VOLUME - CLOSED_VOLUME) * openness) * falloff;
    }

    /* first tick happens after the sound starts, so set the muffle right away too */
    public void applyStartMuffle() {
        float muffle = ExteriorHumSoundInstance.CLOSED_MUFFLE;
        SoundMuffle.set(this, muffle + (1.0F - muffle) * openness);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
