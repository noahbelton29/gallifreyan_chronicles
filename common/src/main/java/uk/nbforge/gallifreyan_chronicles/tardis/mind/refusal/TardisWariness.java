package uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.FearLevel;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Handles control refusal based on the TARDIS's trust in the player.
 *
 * <p>Low trust makes controls more likely to stick or refuse to move.
 * Repeated attempts can gradually improve trust and reduce the chance of refusal.
 */
public final class TardisWariness {

    /**
     * Trust below this value causes controls to always refuse.
     */
    public static final float HOSTILE = 5f;

    /**
     * Trust below this value causes controls to have a higher chance of refusing.
     */
    public static final float WARY = 20f;

    /**
     * Trust below this value causes a small chance of refusal.
     */
    public static final float CAUTIOUS = 35f;

    /**
     * Maximum chance of a control sticking while the TARDIS is wary.
     */
    private static final float WARY_MAX_JAM = 0.6f;

    /**
     * Minimum chance of a control sticking while the TARDIS is wary.
     */
    private static final float WARY_MIN_JAM = 0.25f;

    /**
     * Chance of a control sticking while the TARDIS is cautious.
     */
    private static final float CAUTIOUS_JAM = 0.1f;

    /**
     * Trust gained when a control sticks, if the TARDIS is not afraid.
     */
    private static final float TRUST_PER_JAM = 2f;

    private TardisWariness() {
    }

    /**
     * Checks whether a TARDIS control refuses to move.
     *
     * <p>Refusal depends on the TARDIS's trust in the player and whether
     * the TARDIS is currently terrified of them.
     *
     * @param control control entity being used
     * @param console console containing the control
     * @param player  player attempting to use the control
     * @return {@code true} if the control refuses to move
     */
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

        /* afraid of you: controls stick more even if it trusts you */
        float chance = jamChance(trust);
        if (TardisMindSystem.fearLevel(tardis, player.getUUID()).atLeast(FearLevel.AFRAID))
            chance = Math.min(1f, chance + AFRAID_JAM);

        if (level.getRandom().nextFloat() >= chance) return false;

        level.playSound(null, control.blockPosition(), GCSounds.LEVER, SoundSource.BLOCKS, 0.7F, 0.5F);
        level.sendParticles(ParticleTypes.SMOKE, control.getX(), control.getY() + 0.1, control.getZ(), 4, 0.05, 0.05, 0.05, 0.01);
        MindMessages.say(player, ChatFormatting.GRAY, trust < HOSTILE ? "tardis_controls_frozen" : "tardis_control_sticks");

        TardisDiscovery.reveal(tardis, MindEnums.Reveal.ITS_CONTROLS_STICK);

        // A successful refusal can improve trust, unless the TARDIS is afraid of the player.
        if (trust < CAUTIOUS && !TardisMindSystem.afraidOf(tardis, player.getUUID())) {
            TardisMindSystem.addTrust(tardis, player.getUUID(), TRUST_PER_JAM);
        }

        return true;
    }

    /**
     * Calculates the chance of a control sticking based on the player's trust.
     *
     * <p>Hostile trust always causes refusal, wary trust gradually reduces
     * the chance, cautious trust has a small fixed chance, and higher trust
     * prevents controls from sticking.
     *
     * @param trust current trust value
     * @return chance of the control sticking, between {@code 0.0} and {@code 1.0}
     */
    private static final float AFRAID_JAM = 0.2f;

    public static float jamChance(float trust) {
        if (trust < HOSTILE) return 1f;

        // Interpolate between the two chances as trust improves.
        if (trust < WARY) {
            return Mth.lerp((trust - HOSTILE) / (WARY - HOSTILE), WARY_MAX_JAM, WARY_MIN_JAM);
        }

        if (trust < CAUTIOUS) return CAUTIOUS_JAM;

        return 0f;
    }
}