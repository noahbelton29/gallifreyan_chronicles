package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;

public final class TardisFeedback {
    public enum Cause {
        NORMAL,
        ABUSE,
        BETRAYAL
    }

    private static final float MIN_CHANGE = 0.5f;

    private static final float MODERATE_LOSS = 3f;

    private static final float MAJOR_CHANGE = 8f;

    private static final int MAX_PARTICLES = 6;

    private static final long COOLDOWN_TICKS = 20L;

    private static final double CONSOLE_HEIGHT = 1.1;

    private static final Map<UUID, Long> LAST = ServerState.track(new HashMap<>());

    private TardisFeedback() {}

    public static void reset() {
        LAST.clear();
    }

    public static void trustChanged(Tardis tardis, float change, Cause cause) {
        float size = Math.abs(change);
        boolean serious = cause != Cause.NORMAL;
        if (size < MIN_CHANGE && !serious) return;

        long now = TardisMinds.now(tardis.server());
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
    }

    private static void emit(
            ServerLevel level, BlockPos pos, double height, int count, boolean gained, float size, Cause cause) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + height;
        double z = pos.getZ() + 0.5;

        if (gained) {
            level.sendParticles(ParticleTypes.HEART, x, y, z, count, 0.35, 0.15, 0.35, 0.0);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6f, 1.3f);
            level.sendParticles(ParticleTypes.WAX_OFF, x, y, z, count, 0.4, 0.2, 0.4, 0.02);
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
