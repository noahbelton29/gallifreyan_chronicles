package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.mojang.blaze3d.audio.Channel;
import com.noahtnt2009.gallifreyan_chronicles.client.sound.OffsetChannel;
import net.minecraft.client.sounds.AudioStream;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
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

    @Override
    public void gc$setOffsetSeconds(float seconds) {
        this.gc$offsetSeconds = seconds;
    }

    @Inject(method = "play", at = @At("HEAD"))
    private void gc$applyOffset(CallbackInfo ci) {
        if (gc$offsetSeconds > 0.0F && stream == null) {
            AL10.alSourcef(source, AL11.AL_SEC_OFFSET, gc$offsetSeconds);
        }
        gc$offsetSeconds = 0.0F;
    }
}
