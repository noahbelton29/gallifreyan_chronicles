package com.noahtnt2009.gallifreyan_chronicles.client.sound;

import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.Music;

public final class GCMusic {
    private static final int MIN_DELAY = 20;
    private static final int MAX_DELAY = 600;

    public static final Music TITLE = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GCSounds.TITLE_THEME),
            MIN_DELAY, MAX_DELAY, true);

    private GCMusic() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null) return;
        MusicManager music = minecraft.getMusicManager();
        if (music.isPlayingMusic(TITLE)) {
            music.stopPlaying(TITLE);
        }
    }
}
