package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.flight;

import com.noahtnt2009.gallifreyan_chronicles.util.GCSparks;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisCrashRumblePayload;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisCrash;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/* a stressed tardis flies badly. the more stressed, the harder it shakes.
   flying while stressed wears it out more, and if it reaches breaking point mid flight it crashes */
public final class TardisTurbulence {
    /* no shaking below this */
    private static final float CALM_BELOW = 30f;
    /* flying while this stressed makes it worse */
    private static final float STRAIN_FROM = 50f;
    private static final float STRAIN_PER_SECOND = 0.5f;
    /* reaching this mid flight makes it crash */
    private static final float CRASH_AT = 95f;
    /* how stressed it's left after a stress crash. worn out, not calm */
    private static final float AFTER_CRASH = 60f;

    private static final float CREAKS_FROM = 60f;
    private static final float SPARKS_FROM = 75f;
    private static final float SPARK_CHANCE = 0.35f;
    private static final int CHECK_TICKS = 20;

    private static final Set<UUID> FLYING = new HashSet<>();

    private TardisTurbulence() {
    }

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisTurbulence::onTakeOff);
    }

    public static void reset() {
        FLYING.clear();
    }

    /* the vworp goes higher pitched once it's past 60 stress, up to 1.3 at 100 */
    private static final float HIGH_VWORP_FROM = 60f;
    private static final float MAX_VWORP_PITCH = 1.3f;

    public static float vworpPitch(float stress) {
        float t = Mth.clamp((stress - HIGH_VWORP_FROM) / (100f - HIGH_VWORP_FROM), 0f, 1f);
        return 1f + t * (MAX_VWORP_PITCH - 1f);
    }

    /* 0 when calm, curving up to 1 at 100 stress. gentle at first, violent near the top */
    public static float intensity(float stress) {
        float t = Mth.clamp((stress - CALM_BELOW) / (100f - CALM_BELOW), 0f, 1f);
        return t * t;
    }

    private static void onTakeOff(Tardis tardis) {
        float intensity = intensity(TardisMindSystem.stress(tardis));
        atConsole(tardis, (level, console) -> TardisCrashRumblePayload.broadcastStress(level, console, intensity));

        if (FLYING.add(tardis.id())) schedule(tardis.server(), tardis.id());
    }

    private static void schedule(MinecraftServer server, UUID id) {
        GCScheduler.runLater(server, CHECK_TICKS, () -> tick(server, id));
    }

    private static void tick(MinecraftServer server, UUID id) {
        Tardis tardis = TardisApi.get(server, id).orElse(null);
        boolean flying = tardis != null && tardis.data().getFlightStart(id) != null && !TardisCrash.isCrashing(tardis);
        if (!flying) {
            FLYING.remove(id);
            return;
        }

        float stress = TardisMindSystem.stress(tardis);
        if (stress >= STRAIN_FROM) {
            TardisMindSystem.addStress(tardis, STRAIN_PER_SECOND * CHECK_TICKS / 20f);
            stress = TardisMindSystem.stress(tardis);
        }

        if (stress >= CRASH_AT && inVortex(tardis) && crash(tardis)) {
            FLYING.remove(id);
            return;
        }

        shake(tardis, stress);
        schedule(server, id);
    }

    private static void shake(Tardis tardis, float stress) {
        float intensity = intensity(stress);
        if (intensity <= 0f) return;

        atConsole(tardis, (level, console) -> {
            RandomSource random = level.getRandom();
            /* a steady rumble that smoothly gets worse with stress. at 100 it's as hard as a crash */
            TardisCrashRumblePayload.broadcastStress(level, console, intensity);
            BlockPos pos = BlockPos.containing(console);
            if (stress >= CREAKS_FROM && random.nextFloat() < intensity) {
                level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.4f + intensity * 0.6f, 0.5f);
            }
            /* the odd crackle, not constant bursts. the shaking does the heavy lifting */
            if (stress >= SPARKS_FROM && random.nextFloat() < intensity * SPARK_CHANCE) {
                Vec3 spot = console.add((random.nextDouble() - 0.5) * 1.5, 1, (random.nextDouble() - 0.5) * 1.5);
                GCSparks.small(level, spot, 4 + Math.round(8 * intensity));
            }
        });
    }

    private static boolean crash(Tardis tardis) {
        /* use the console inside, not one linked from somewhere else */
        ServerLevel interior = tardis.interiorLevel().orElse(null);
        TardisConsoleBlockEntity console = tardis.consoles().stream()
                .filter(each -> each.getLevel() == interior)
                .findFirst()
                .orElseGet(() -> tardis.console().orElse(null));
        if (console == null || !FlightLeverControl.emergencyLand(console, "crash_landing_stress")) return false;
        TardisMindSystem.setStress(tardis, AFTER_CRASH);
        return true;
    }

    private static boolean inVortex(Tardis tardis) {
        return tardis.exterior().map(exterior -> exterior.getFlightState() == TardisFlightState.IN_VORTEX).orElse(false);
    }

    private interface AtConsole {
        void run(ServerLevel level, Vec3 console);
    }

    private static void atConsole(Tardis tardis, AtConsole run) {
        ServerLevel level = tardis.interiorLevel().orElse(null);
        BlockPos console = tardis.consolePos().orElse(null);
        if (level != null && console != null) run.run(level, Vec3.atCenterOf(console));
    }
}
