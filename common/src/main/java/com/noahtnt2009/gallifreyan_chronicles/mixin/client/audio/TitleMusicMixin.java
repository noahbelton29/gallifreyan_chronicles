package com.noahtnt2009.gallifreyan_chronicles.mixin.client.audio;

import com.noahtnt2009.gallifreyan_chronicles.client.sound.GCMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class TitleMusicMixin {
    @Inject(method = "getSituationalMusic", at = @At("RETURN"), cancellable = true)
    private void gc$useTitleTheme(CallbackInfoReturnable<Music> cir) {
        if (cir.getReturnValue() == Musics.MENU) {
            cir.setReturnValue(GCMusic.TITLE);
        }
    }
}
