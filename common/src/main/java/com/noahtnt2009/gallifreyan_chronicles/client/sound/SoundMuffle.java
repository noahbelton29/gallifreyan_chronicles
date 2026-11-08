package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.mixin.client.audio.SoundEngineAccessor;
import com.noahtnt2009.gallifreyan_chronicles.mixin.client.audio.SoundManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;

/* muffles a playing sound (like hearing it through a wall).
   1 = clear, lower = more muffled */
public final class SoundMuffle {
    private SoundMuffle() {
    }

    public static void set(SoundInstance sound, float highGain) {
        var engine = ((SoundManagerAccessor) Minecraft.getInstance().getSoundManager()).gc$getSoundEngine();
        ChannelAccess.ChannelHandle handle = ((SoundEngineAccessor) engine).gc$getInstanceToChannel().get(sound);
        if (handle == null) return;
        handle.execute(channel -> ((OffsetChannel) channel).gc$setMuffle(highGain));
    }
}
