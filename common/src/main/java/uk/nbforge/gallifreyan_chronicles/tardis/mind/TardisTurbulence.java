package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisCrashRumblePayload;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlight;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;
import uk.nbforge.gallifreyan_chronicles.util.SparkUtil;

public final class TardisTurbulence {
    private static final float CALM_BELOW = 30f;
    private static final float STRAIN_FROM = 50f;
    private static final float STRAIN_PER_SECOND = 0.5f;
    private static final float CRASH_AT = 95f;
    private static final float AFTER_CRASH = 60f;

    private static final float CREAKS_FROM = 60f;
    private static final float SPARKS_FROM = 75f;
    private static final float SPARK_CHANCE = 0.35f;
    private static final int CHECK_TICKS = 20;

    private static final Set<UUID> FLYING = ServerState.track(new HashSet<>());

    private TardisTurbulence() {}

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisTurbulence::onTakeOff);
    }

    public static void reset() {
        FLYING.clear();
    }

    private static final float HIGH_VWORP_FROM = 60f;
    private static final float MAX_VWORP_PITCH = 1.3f;

    public static float vworpPitch(float stress) {
        float t = Mth.clamp((stress - HIGH_VWORP_FROM) / (100f - HIGH_VWORP_FROM), 0f, 1f);
        return 1f + t * (MAX_VWORP_PITCH - 1f);
    }

    /** 0 when calm, curves up to 1 at 100 stress, gentle at first and violent near the top */
    public static float intensity(float stress) {
        float t = Mth.clamp((stress - CALM_BELOW) / (100f - CALM_BELOW), 0f, 1f);
        return t * t;
    }

    private static void onTakeOff(Tardis tardis) {
        float intensity = intensity(TardisMinds.stress(tardis));
        atConsole(tardis, (level, console) -> TardisCrashRumblePayload.broadcastStress(level, console, intensity));

        if (FLYING.add(tardis.id())) schedule(tardis.server(), tardis.id());
    }

    private static void schedule(MinecraftServer server, UUID id) {
        SchedulerUtil.runLater(server, CHECK_TICKS, () -> tick(server, id));
    }

    private static void tick(MinecraftServer server, UUID id) {
        Tardis tardis = Tardis.get(server, id).orElse(null);
        boolean flying = tardis != null && tardis.data().getFlightStart() != null && !TardisCrash.isCrashing(tardis);
        if (!flying) {
            FLYING.remove(id);
            return;
        }

        float stress = TardisMinds.stress(tardis);
        if (stress >= STRAIN_FROM) {
            TardisMinds.addStress(tardis, STRAIN_PER_SECOND * CHECK_TICKS / 20f);
            stress = TardisMinds.stress(tardis);
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
            TardisCrashRumblePayload.broadcastStress(level, console, intensity);
            BlockPos pos = BlockPos.containing(console);
            if (stress >= CREAKS_FROM && random.nextFloat() < intensity) {
                level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.4f + intensity * 0.6f, 0.5f);
            }
            if (stress >= SPARKS_FROM && random.nextFloat() < intensity * SPARK_CHANCE) {
                Vec3 spot = console.add((random.nextDouble() - 0.5) * 1.5, 1, (random.nextDouble() - 0.5) * 1.5);
                SparkUtil.small(level, spot, 4 + Math.round(8 * intensity));
            }
        });
    }

    private static boolean crash(Tardis tardis) {
        ServerLevel interior = tardis.interiorLevel().orElse(null);
        TardisConsoleBlockEntity console = tardis.consoles().stream()
                .filter(each -> each.getLevel() == interior)
                .findFirst()
                .orElseGet(() -> tardis.console().orElse(null));
        boolean landed = console != null && TardisFlight.emergencyLand(console, "crash_landing_stress");
        if (landed) TardisMinds.setStress(tardis, AFTER_CRASH);
        return landed;
    }

    private static boolean inVortex(Tardis tardis) {
        return tardis.exterior()
                .map(exterior -> exterior.travel().getFlightState() == TardisFlightState.IN_VORTEX)
                .orElse(false);
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
