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
    public static final double HEARING_RANGE = 30.0;
    private static final float IN_RANGE_VOLUME = .75F;

    private static final float FADE_PER_TICK = 0.05F;

    private final BooleanSupplier stillValid;
    private final BooleanSupplier powered;
    private float fade;
    private int tickCount = 0;

    public TardisConsoleHumSoundInstance(BlockPos pos, BooleanSupplier stillValid, BooleanSupplier powered) {
        super(GCSounds.FIRST_DOCTORS_HUM, SoundSource.BLOCKS, RandomSource.create());

        this.stillValid = stillValid;
        this.powered = powered;
        this.fade = powered.getAsBoolean() ? 1.0F : 0.0F;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;

        this.looping = true;
        this.delay = 0;
        this.volume = IN_RANGE_VOLUME * fade;
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

        float target = powered.getAsBoolean() ? 1.0F : 0.0F;
        if (fade < target) fade = Math.min(target, fade + FADE_PER_TICK);
        else if (fade > target) fade = Math.max(target, fade - FADE_PER_TICK);

        double distance = player.position().distanceTo(
                new Vec3(this.x, this.y, this.z)
        );

        if (distance >= HEARING_RANGE) {
            this.volume = 0.0F;
        } else {
            float attenuation = (float) (1.0 - distance / HEARING_RANGE);
            this.volume = IN_RANGE_VOLUME * attenuation;
        }
        this.volume *= fade;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public int getTickCount() {
        return tickCount;
    }

    public void setTickCount(int tickCount) {
        this.tickCount = tickCount;
    }
}