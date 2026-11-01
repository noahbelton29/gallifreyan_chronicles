package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.mojang.blaze3d.audio.Channel;
import com.noahtnt2009.gallifreyan_chronicles.client.sound.OffsetChannel;
import com.noahtnt2009.gallifreyan_chronicles.client.sound.OffsetSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(
            method = "lambda$play$0(FFLnet/minecraft/client/resources/sounds/SoundInstance;FZZLnet/minecraft/world/phys/Vec3;Lcom/mojang/blaze3d/audio/Channel;)V",
            at = @At("HEAD")
    )
    private static void gc$offset(float pitch, float volume, SoundInstance instance, float attenuation,
                                  boolean looping, boolean streaming, Vec3 pos, Channel channel, CallbackInfo ci) {
        float offset = instance instanceof OffsetSoundInstance offsetInstance ? offsetInstance.offsetSeconds() : 0.0F;
        ((OffsetChannel) channel).gc$setOffsetSeconds(offset);
    }
}
