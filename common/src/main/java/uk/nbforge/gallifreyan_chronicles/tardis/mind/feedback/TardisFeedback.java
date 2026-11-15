package uk.nbforge.gallifreyan_chronicles.tardis.mind.feedback;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
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
   Shows how a TARDIS feels with particles around its console and exterior.
   Gaining trust shows hearts and losing it shows frustration or distress.
   A cooldown limits small changes so the room does not fill with particles.
*/
public final class TardisFeedback {
    /** What made the trust change. */
    public enum Cause {
        NORMAL,
        ABUSE,
        BETRAYAL
    }

    // Small changes are ignored unless something serious happened
    private static final float MIN_CHANGE = 0.5f;

    // A loss this big makes it show it is unhappy
    private static final float MODERATE_LOSS = 3f;

    // A change this big gives a stronger reaction
    private static final float MAJOR_CHANGE = 8f;

    // Most particles a normal reaction makes
    private static final int MAX_PARTICLES = 6;

    // Wait between smaller reactions
    private static final long COOLDOWN_TICKS = 20L;

    // How high above the console the particles spawn
    private static final double CONSOLE_HEIGHT = 1.1;

    // When each TARDIS last showed a reaction
    private static final Map<UUID, Long> LAST = new HashMap<>();

    private TardisFeedback() {
    }

    public static void reset() {
        LAST.clear();
    }

    /**
       The bigger the change the more particles.
       Serious things like abuse and betrayal react straight away even if it showed one recently.
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

        // Only at the console inside, never on the box outside
    }

    /**
       Gains give hearts, losses give other particles depending on how bad it was.
       Betrayal gets its own stronger reaction and small losses a gentler one.
    */
    private static void emit(ServerLevel level, BlockPos pos, double height, int count, boolean gained,
                             float size, Cause cause) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + height;
        double z = pos.getZ() + 0.5;

        if (gained) {
            level.sendParticles(ParticleTypes.HEART, x, y, z, count, 0.35, 0.15, 0.35, 0.0);
            // A soft warm chime so being kind feels nice
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