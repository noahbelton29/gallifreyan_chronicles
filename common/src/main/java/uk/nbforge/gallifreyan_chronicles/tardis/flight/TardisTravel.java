package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;

public final class TardisTravel {
    private static final double SCALE = 0.385;
    private static final double POWER = 0.557;
    private static final double MIN_SECONDS = 3.0;
    private static final double DIMENSION_SECONDS = 15.0;

    private TardisTravel() {
    }

    public static void init() {
        TardisEvents.TAKE_OFF.register(tardis -> tardis.data().setFlightStart(tardis.id(), now(tardis.server())));
        TardisEvents.LAND.register(tardis -> tardis.data().setFlightStart(tardis.id(), null));
    }

    private static long now(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    public static double secondsFor(double blocks) {
        return Math.max(MIN_SECONDS, SCALE * Math.pow(blocks, POWER));
    }

    public static long requiredTicks(Tardis tardis) {
        LandingTarget target = tardis.landingTarget().orElse(null);
        BlockPos from = tardis.exteriorPos().orElse(null);
        if (target == null || from == null) return 0L;

        double blocks = Math.sqrt(from.distSqr(target.pos()));
        double seconds = secondsFor(blocks);
        boolean changesDimension = !target.dimension().equals(tardis.exteriorDimension());
        if (changesDimension) {
            seconds += DIMENSION_SECONDS;
        }
        return Math.round(seconds * 20.0);
    }

    public static long vortexTicks(Tardis tardis) {
        Long start = tardis.data().getFlightStart(tardis.id());
        if (start == null) return Long.MAX_VALUE / 2;
        return Math.max(0L, now(tardis.server()) - start - FlightTimings.TAKEOFF_TICKS);
    }

    public static long remainingTicks(Tardis tardis) {
        return Math.max(0L, requiredTicks(tardis) - vortexTicks(tardis));
    }

    public static float progress(Tardis tardis) {
        long required = requiredTicks(tardis);
        if (required <= 0L) return 1F;
        return Mth.clamp(vortexTicks(tardis) / (float) required, 0F, 1F);
    }

    public static LandingTarget crashTarget(Tardis tardis) {
        return partWayTarget(tardis, 0F);
    }

    /* where it ends up if it drops out early. atLeast: never less than this far along */
    public static LandingTarget partWayTarget(Tardis tardis, float atLeast) {
        LandingTarget target = tardis.landingTarget().orElse(null);
        BlockPos from = tardis.exteriorPos().orElse(BlockPos.ZERO);
        if (target == null) return new LandingTarget(tardis.exteriorDimension(), from, 0F);
        float progress = Math.max(progress(tardis), atLeast);
        BlockPos partWay = between(from, target.pos(), progress);
        boolean pastHalfway = progress >= 0.5F;
        ResourceKey<Level> dimension = pastHalfway ? target.dimension() : tardis.exteriorDimension();
        return new LandingTarget(dimension, partWay, target.yaw());
    }

    private static BlockPos between(BlockPos from, BlockPos to, float progress) {
        return BlockPos.containing(
                Mth.lerp(progress, from.getX(), to.getX()),
                Mth.lerp(progress, from.getY(), to.getY()),
                Mth.lerp(progress, from.getZ(), to.getZ()));
    }
}
