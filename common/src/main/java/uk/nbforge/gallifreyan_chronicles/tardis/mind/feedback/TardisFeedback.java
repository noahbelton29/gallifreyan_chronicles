package uk.nbforge.gallifreyan_chronicles.tardis.mind.feedback;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shows how a TARDIS feels through particles around its console and exterior.
 * <p>
 * Gaining trust causes hearts to appear, while losing trust makes the TARDIS
 * show signs of frustration or distress.
 * <p>
 * Small changes are limited by a cooldown so that frequent interactions
 * don't fill the room with particles.
 */
public final class TardisFeedback {
    /**
     * What caused the TARDIS's trust to change.
     */
    public enum Cause {
        NORMAL,
        ABUSE,
        BETRAYAL
    }

    /**
     * Ignore small trust changes unless something serious has happened.
     */
    private static final float MIN_CHANGE = 0.5f;

    /**
     * A loss this large makes the TARDIS show that it's unhappy.
     */
    private static final float MODERATE_LOSS = 3f;

    /**
     * Trust changes this large trigger a stronger reaction.
     */
    private static final float MAJOR_CHANGE = 8f;

    /**
     * The most particles a normal reaction can produce.
     */
    private static final int MAX_PARTICLES = 6;

    /**
     * How long to wait between smaller particle reactions.
     */
    private static final long COOLDOWN_TICKS = 20L;

    /**
     * How far above the console to spawn the particles.
     */
    private static final double CONSOLE_HEIGHT = 1.1;


    /**
     * Remembers when each TARDIS last showed a reaction.
     */
    private static final Map<UUID, Long> LAST = new HashMap<>();

    private TardisFeedback() {
    }

    public static void reset() {
        LAST.clear();
    }

    /**
     * Called whenever a TARDIS gains or loses trust.
     * <p>
     * The size of the change determines how many particles appear.
     * Serious events like abuse and betrayal can trigger a reaction
     * straight away, even if the TARDIS has recently shown one.
     *
     * @param tardis the TARDIS whose trust has changed
     * @param change how much trust was gained or lost
     * @param cause  what caused the change
     */
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

        /* only at the console inside, never on the box outside */
    }

    /**
     * Spawns the particles that show how the TARDIS feels about the change.
     * <p>
     * Gaining trust produces hearts. Losing trust produces different particles
     * depending on how badly the TARDIS has been affected.
     * <p>
     * Betrayal gets its own stronger reaction, while smaller losses produce
     * a more subtle effect.
     *
     * @param level  the dimension where the particles should appear
     * @param pos    the block position to spawn them around
     * @param height how far above the block the particles should appear
     * @param count  how many particles to spawn
     * @param gained whether the TARDIS gained trust
     * @param size   how much trust changed
     * @param cause  what caused the change
     */
    private static void emit(ServerLevel level, BlockPos pos, double height, int count, boolean gained,
                             float size, Cause cause) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + height;
        double z = pos.getZ() + 0.5;

        if (gained) {
            level.sendParticles(ParticleTypes.HEART, x, y, z, count, 0.35, 0.15, 0.35, 0.0);
            /* a soft warm chime so being kind feels nice */
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 1.3F);
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