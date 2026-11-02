package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class ExteriorSounds {
    private ExteriorSounds() {
    }

    public static void locked(Level level, BlockPos pos) {
        play(level, pos, GCSounds.TARDIS_IS_LOCKED);
    }

    public static void keyTurn(Level level, BlockPos pos, boolean nowLocked) {
        play(level, pos, nowLocked ? GCSounds.TARDIS_DOOR_LOCK : GCSounds.TARDIS_DOOR_UNLOCK);
    }

    public static void door(Level level, BlockPos pos, boolean opening) {
        play(level, pos, opening ? GCSounds.TARDIS_DOOR_OPEN : GCSounds.TARDIS_DOOR_CLOSE);
    }

    public static void keyJammed(Level level, BlockPos pos) {
        play(level, pos, GCSounds.TARDIS_KEY_JAMMED);
    }

    private static void play(Level level, BlockPos pos, SoundEvent sound) {
        if (level == null) return;
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
