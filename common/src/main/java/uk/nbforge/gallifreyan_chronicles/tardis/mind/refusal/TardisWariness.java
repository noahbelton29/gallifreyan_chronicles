package uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.FearLevel;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

/**
 * Controls refuse or stick depending on how much the TARDIS trusts the player.
 * Low trust makes controls stick more, and repeated tries slowly build trust and lower the chance.
 */
public final class TardisWariness {

    // Below this trust controls always refuse
    public static final float HOSTILE = 5f;

    // Below this trust controls are much more likely to refuse
    public static final float WARY = 20f;

    // Below this trust there is a small chance of refusal
    public static final float CAUTIOUS = 35f;

    // Highest chance of a control sticking while wary
    private static final float WARY_MAX_JAM = 0.6f;

    // Lowest chance of a control sticking while wary
    private static final float WARY_MIN_JAM = 0.25f;

    // Chance of a control sticking while cautious
    private static final float CAUTIOUS_JAM = 0.1f;

    // Trust gained when a control sticks, if it is not afraid
    private static final float TRUST_PER_JAM = 2f;

    private TardisWariness() {}

    /** Refusal depends on the trust and on whether the TARDIS is terrified of the player. */
    public static boolean jams(TardisControlEntity control, TardisConsoleBlockEntity console, Player player) {
        if (!(control.level() instanceof ServerLevel level)) return false;

        Tardis tardis = TardisApi.of(console).orElse(null);

        if (tardis == null) return false;

        if (TardisTerror.compliant(tardis, player.getUUID())) return false;

        if (TardisTerror.terrified(tardis, player.getUUID())) {
            level.playSound(null, control.blockPosition(), GCSounds.LEVER, SoundSource.BLOCKS, 0.5F, 0.4F);
            MindMessages.say(player, ChatFormatting.DARK_GRAY, "tardis_terrified");
            return true;
        }

        float trust = TardisMindSystem.trust(tardis, player.getUUID());

        // Afraid of you, controls stick more even if it trusts you
        float chance = jamChance(trust);
        if (TardisMindSystem.fearLevel(tardis, player.getUUID()).atLeast(FearLevel.AFRAID))
            chance = Math.min(1f, chance + AFRAID_JAM);

        if (level.getRandom().nextFloat() >= chance) return false;

        level.playSound(null, control.blockPosition(), GCSounds.LEVER, SoundSource.BLOCKS, 0.7F, 0.5F);
        level.sendParticles(
                ParticleTypes.SMOKE, control.getX(), control.getY() + 0.1, control.getZ(), 4, 0.05, 0.05, 0.05, 0.01);
        MindMessages.say(
                player, ChatFormatting.GRAY, trust < HOSTILE ? "tardis_controls_frozen" : "tardis_control_sticks");

        TardisDiscovery.reveal(tardis, MindEnums.Reveal.ITS_CONTROLS_STICK);

        // A refusal can build trust unless the TARDIS is afraid of the player
        if (trust < CAUTIOUS && !TardisMindSystem.afraidOf(tardis, player.getUUID())) {
            TardisMindSystem.addTrust(tardis, player.getUUID(), TRUST_PER_JAM);
        }

        return true;
    }

    // Chance of a control sticking while the TARDIS is afraid of the player
    private static final float AFRAID_JAM = 0.2f;

    public static float jamChance(float trust) {
        if (trust < HOSTILE) return 1f;

        // Moves between the two chances as trust improves
        if (trust < WARY) {
            return Mth.lerp((trust - HOSTILE) / (WARY - HOSTILE), WARY_MAX_JAM, WARY_MIN_JAM);
        }

        if (trust < CAUTIOUS) return CAUTIOUS_JAM;

        return 0f;
    }
}
