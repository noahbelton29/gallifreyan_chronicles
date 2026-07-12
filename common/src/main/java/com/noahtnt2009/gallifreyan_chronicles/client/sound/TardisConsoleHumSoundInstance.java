package com.noahtnt2009.gallifreyan_chronicles.client.sound;

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

    public TardisConsoleHumSoundInstance(BlockPos pos, BooleanSupplier stillValid) {
        super(GCSounds.FIRST_DOCTORS_HUM, SoundSource.BLOCKS, net.minecraft.util.RandomSource.create());
        this.stillValid = stillValid;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
    }

    @Override
    public void tick() {
        if (!stillValid.getAsBoolean()) {
            this.stop();
        }
    }
}
