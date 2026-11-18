package uk.nbforge.gallifreyan_chronicles.tardis.console;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;

public final class ConsoleSounds {
    private ConsoleSounds() {}

    public static void at(ServerLevel level, BlockPos pos, SoundEvent sound, float pitch) {
        TimedSounds.play(level, pos, sound, SoundSource.BLOCKS, 1.0f, pitch);
    }

    public static void atConsole(TardisConsoleBlockEntity console, SoundEvent sound) {
        if (console.getLevel() instanceof ServerLevel level) at(level, console.getBlockPos(), sound, 1.0f);
    }

    public static void atAllConsoles(TardisConsoleBlockEntity console, SoundEvent sound) {
        for (TardisConsoleBlockEntity each : console.linkedConsoles()) {
            atConsole(each, sound);
        }
    }

    public static void atExterior(TardisConsoleBlockEntity console, SoundEvent sound) {
        Tardis.of(console).flatMap(Tardis::exterior).ifPresent(exterior -> {
            if (exterior.getLevel() instanceof ServerLevel level) at(level, exterior.getBlockPos(), sound, 1.0f);
        });
    }

    public static void everywhere(TardisConsoleBlockEntity console, SoundEvent sound) {
        atAllConsoles(console, sound);
        atExterior(console, sound);
    }
}
