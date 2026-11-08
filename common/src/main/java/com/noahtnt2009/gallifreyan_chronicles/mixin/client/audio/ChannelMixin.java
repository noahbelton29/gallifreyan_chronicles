package com.noahtnt2009.gallifreyan_chronicles.mixin.client.audio;

import com.mojang.blaze3d.audio.Channel;
import com.noahtnt2009.gallifreyan_chronicles.client.sound.OffsetChannel;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Channel.class)
public abstract class ChannelMixin implements OffsetChannel {
    @Shadow
    @Final
    private int source;

    @Shadow
    private AudioStream stream;

    @Unique
    private float gc$offsetSeconds;

    /* openal low-pass filter for muffling, made the first time it's needed */
    @Unique
    private int gc$filter;

    @Override
    public void gc$setOffsetSeconds(float seconds) {
        this.gc$offsetSeconds = seconds;
    }

    @Override
    public void gc$setMuffle(float highGain) {
        try {
            if (highGain >= 0.999F) {
                if (gc$filter != 0) AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_FILTER_NULL);
                return;
            }
            if (gc$filter == 0) {
                gc$filter = EXTEfx.alGenFilters();
                EXTEfx.alFilteri(gc$filter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
            }
            EXTEfx.alFilterf(gc$filter, EXTEfx.AL_LOWPASS_GAIN, 1.0F);
            EXTEfx.alFilterf(gc$filter, EXTEfx.AL_LOWPASS_GAINHF, Math.max(0.0F, highGain));
            AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, gc$filter);
        } catch (Throwable ignored) {
            /* no efx on this sound card, it just won't be muffled */
        }
    }

    @Inject(method = "destroy", at = @At("HEAD"), require = 0)
    private void gc$deleteFilter(CallbackInfo ci) {
        if (gc$filter != 0) {
            try {
                EXTEfx.alDeleteFilters(gc$filter);
            } catch (Throwable ignored) {
            }
            gc$filter = 0;
        }
    }

    @Inject(method = "play", at = @At("HEAD"), require = 0)
    private void gc$applyOffset(CallbackInfo ci) {
        if (gc$offsetSeconds > 0.0F && stream == null) {
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, gc$offsetSeconds);
        }
        gc$offsetSeconds = 0.0F;
    }
}
