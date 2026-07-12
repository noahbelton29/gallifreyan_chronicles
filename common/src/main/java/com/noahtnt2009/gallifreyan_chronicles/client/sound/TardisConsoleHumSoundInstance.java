package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

import java.util.function.BooleanSupplier;

/**
 * Looping ambient hum tied to a single TardisConsoleBlockEntity's lifetime.
 * Client-only. Started once from TardisConsoleBlockEntity#setLevel and
 * self-stops via #isStillValid once the block entity is removed/unloaded.
 */
public class TardisConsoleHumSoundInstance extends AbstractTickableSoundInstance {
    private final BooleanSupplier stillValid;
    private int tickCount = 0;

    public TardisConsoleHumSoundInstance(BlockPos pos, BooleanSupplier stillValid) {
        super(GCSounds.FIRST_DOCTORS_HUM, SoundSource.BLOCKS, net.minecraft.util.RandomSource.create());
        this.stillValid = stillValid;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        Constants.LOG.info("TardisConsoleHumSoundInstance: constructed, sound={}, source={}, canPlaySound={}",
                GCSounds.FIRST_DOCTORS_HUM.location(), SoundSource.BLOCKS, this.canPlaySound());
    }

    @Override
    public void tick() {
        if (tickCount == 0) {
            Constants.LOG.info("TardisConsoleHumSoundInstance: first tick() reached - engine accepted the instance");
        }
        tickCount++;
        if (!stillValid.getAsBoolean()) {
            Constants.LOG.info("TardisConsoleHumSoundInstance: stopping after {} ticks", tickCount);
            this.stop();
        }
    }
}
