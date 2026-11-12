package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.feedback;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/* shows players how a tardis feels. hearts for trust gained, smoke and sparks for trust lost.
   small changes are rate limited so a busy console never turns into a particle storm */
public final class TardisFeedback {
    public enum Cause {
        NORMAL, ABUSE, BETRAYAL
    }

    private static final float MIN_CHANGE = 0.5f;
    private static final float MODERATE_LOSS = 3f;
    private static final float MAJOR_CHANGE = 8f;
    private static final int MAX_PARTICLES = 6;
    private static final long COOLDOWN_TICKS = 20L;
    private static final double CONSOLE_HEIGHT = 1.1;
    private static final double EXTERIOR_HEIGHT = 2.2;

    private static final Map<UUID, Long> LAST = new HashMap<>();

    private TardisFeedback() {
    }

    public static void reset() {
        LAST.clear();
    }

    /* change is how much trust really moved, after its traits and the 0 to 100 limits */
    public static void trustChanged(Tardis tardis, float change, Cause cause) {
        float size = Math.abs(change);
        boolean serious = cause != Cause.NORMAL;
        if (size < MIN_CHANGE && !serious) return;

        long now = TardisMindSystem.now(tardis.server());
        boolean major = serious || size >= MAJOR_CHANGE;
        Long last = LAST.get(tardis.id());
        if (!major && last != null && now - last < COOLDOWN_TICKS) return;
        LAST.put(tardis.id(), now);

        int count = Mth.clamp(Math.round(size), 1, MAX_PARTICLES);
        boolean gained = change > 0f;

        ServerLevel interior = tardis.interiorLevel().orElse(null);
        BlockPos console = tardis.consolePos().orElse(null);
        if (interior != null && console != null) {
            emit(interior, console, CONSOLE_HEIGHT, count, gained, size, cause);
        }

        ServerLevel exterior = tardis.exteriorLevel().orElse(null);
        BlockPos outside = tardis.exteriorPos().orElse(null);
        if (exterior != null && outside != null) {
            emit(exterior, outside, EXTERIOR_HEIGHT, Math.max(1, count / 2), gained, size, cause);
        }
    }

    private static void emit(ServerLevel level, BlockPos pos, double height, int count, boolean gained,
                             float size, Cause cause) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + height;
        double z = pos.getZ() + 0.5;

        if (gained) {
            level.sendParticles(ParticleTypes.HEART, x, y, z, count, 0.35, 0.15, 0.35, 0.0);
        } else if (cause == Cause.BETRAYAL) {
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, 8, 0.4, 0.2, 0.4, 0.1);
            level.sendParticles(ParticleTypes.SMOKE, x, y, z, 12, 0.4, 0.3, 0.4, 0.03);
        } else if (cause == Cause.ABUSE || size >= MAJOR_CHANGE) {
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, x, y, z, Math.min(count, 4), 0.35, 0.15, 0.35, 0.05);
            level.sendParticles(ParticleTypes.SMOKE, x, y, z, count + 2, 0.35, 0.2, 0.35, 0.02);
        } else if (size >= MODERATE_LOSS) {
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, Math.min(count, 3), 0.35, 0.15, 0.35, 0.0);
        } else {
            level.sendParticles(ParticleTypes.SMOKE, x, y, z, count + 1, 0.3, 0.1, 0.3, 0.01);
        }
    }
}
