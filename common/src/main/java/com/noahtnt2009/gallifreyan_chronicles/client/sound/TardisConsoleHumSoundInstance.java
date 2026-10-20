package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;

import java.util.function.BooleanSupplier;

public class TardisConsoleHumSoundInstance extends AbstractTickableSoundInstance {
    public static final double HEARING_RANGE = 20.0;
    private static final float IN_RANGE_VOLUME = 1.0F;

    private final BooleanSupplier stillValid;
    private int tickCount = 0;

    public TardisConsoleHumSoundInstance(BlockPos pos, BooleanSupplier stillValid) {
        super(GCSounds.FIRST_DOCTORS_HUM, SoundSource.BLOCKS, RandomSource.create());

        this.stillValid = stillValid;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;

        this.looping = true;
        this.delay = 0;
        this.volume = IN_RANGE_VOLUME;
        this.pitch = 1.0F;
    }

    @Override
    public void tick() {
        tickCount++;

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !stillValid.getAsBoolean()) {
            this.stop();
            return;
        }

        double distance = player.position().distanceTo(
                new Vec3(this.x, this.y, this.z)
        );

        if (distance >= HEARING_RANGE) {
            this.volume = 0.0F;
        } else {
            // Gradually decrease volume with distance.
            float attenuation = (float) (1.0 - distance / HEARING_RANGE);
            this.volume = IN_RANGE_VOLUME * attenuation;
        }
    }

    public int getTickCount() {
        return tickCount;
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }
}