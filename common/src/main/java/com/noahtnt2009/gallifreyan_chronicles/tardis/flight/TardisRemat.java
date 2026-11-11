package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.sound.TimedSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLandingSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;

public final class TardisRemat {
    private TardisRemat() {
    }

    public static boolean vanish(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exterior().orElse(null);
        if (exterior == null) return false;
        exterior.cancelLanding();
        exterior.vanishInstantly();
        exterior.setFlightState(TardisFlightState.IN_VORTEX, 0L);
        return true;
    }

    public static boolean rematerialise(Tardis tardis) {
        if (tardis.landingTarget().isEmpty() || !vanish(tardis)) return false;

        TardisLandingSystem.relocate(tardis.server(), tardis.id());
        TardisExteriorBlockEntity landed = tardis.exterior().orElse(null);
        if (landed == null) return false;

        int fadeInTicks = landed.getFadeType().timing().fadeInTicks();
        landed.scheduleLanding(0);
        landed.setFlightState(TardisFlightState.LANDING, fadeInTicks);
        playLandSound(tardis, landed);
        return true;
    }

    private static void playLandSound(Tardis tardis, TardisExteriorBlockEntity exterior) {
        if (exterior.getLevel() instanceof ServerLevel level) {
            TimedSounds.play(level, exterior.getBlockPos(), GCSounds.TARDIS_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        for (TardisConsoleBlockEntity console : tardis.consoles()) {
            if (console.getLevel() instanceof ServerLevel level) {
                TimedSounds.play(level, console.getBlockPos(), GCSounds.TARDIS_LAND, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }
}
