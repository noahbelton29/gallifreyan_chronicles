package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;

public final class TardisRemat {
    private TardisRemat() {}

    public static boolean vanish(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exterior().orElse(null);
        if (exterior == null) return false;
        exterior.travel().cancelLanding();
        exterior.travel().vanishInstantly();
        exterior.travel().setFlightState(TardisFlightState.IN_VORTEX, 0L);
        return true;
    }

    public static boolean rematerialise(Tardis tardis) {
        if (tardis.landingTarget().isEmpty() || !vanish(tardis)) return false;

        TardisLanding.relocate(tardis);
        TardisExteriorBlockEntity landed = tardis.exterior().orElse(null);
        if (landed == null) return false;

        int fadeInTicks = landed.travel().getFadeType().timing().fadeInTicks();
        landed.travel().scheduleLanding(0);
        landed.travel().setFlightState(TardisFlightState.LANDING, fadeInTicks);
        playLandSound(tardis, landed);
        return true;
    }

    private static void playLandSound(Tardis tardis, TardisExteriorBlockEntity exterior) {
        if (exterior.getLevel() instanceof ServerLevel level) {
            TimedSounds.play(level, exterior.getBlockPos(), GCSounds.TARDIS_LAND, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        for (TardisConsoleBlockEntity console : tardis.consoles()) {
            if (console.getLevel() instanceof ServerLevel level) {
                TimedSounds.play(level, console.getBlockPos(), GCSounds.TARDIS_LAND, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }
}
