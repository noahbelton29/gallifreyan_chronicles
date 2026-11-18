package uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.FearLevel;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisDiscovery;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums;

public final class TardisWariness {

    public static final float HOSTILE = 5f;

    public static final float WARY = 20f;

    public static final float CAUTIOUS = 35f;

    private static final float WARY_MAX_JAM = 0.6f;

    private static final float WARY_MIN_JAM = 0.25f;

    private static final float CAUTIOUS_JAM = 0.1f;

    private static final float TRUST_PER_JAM = 2f;

    private TardisWariness() {}

    public static boolean jams(TardisControlEntity control, TardisConsoleBlockEntity console, Player player) {
        if (!(control.level() instanceof ServerLevel level)) return false;

        Tardis tardis = Tardis.of(console).orElse(null);

        if (tardis == null) return false;

        if (TardisTerror.compliant(tardis, player.getUUID())) return false;

        if (TardisTerror.terrified(tardis, player.getUUID())) {
            level.playSound(null, control.blockPosition(), GCSounds.LEVER, SoundSource.BLOCKS, 0.5f, 0.4f);
            MindMessages.say(player, ChatFormatting.DARK_GRAY, "tardis_terrified");
            return true;
        }

        float trust = TardisMinds.trust(tardis, player.getUUID());

        float chance = jamChance(trust);
        if (TardisMinds.fearLevel(tardis, player.getUUID()).atLeast(FearLevel.AFRAID))
            chance = Math.min(1f, chance + AFRAID_JAM);

        if (level.getRandom().nextFloat() >= chance) return false;

        level.playSound(null, control.blockPosition(), GCSounds.LEVER, SoundSource.BLOCKS, 0.7f, 0.5f);
        level.sendParticles(
                ParticleTypes.SMOKE, control.getX(), control.getY() + 0.1, control.getZ(), 4, 0.05, 0.05, 0.05, 0.01);
        MindMessages.say(
                player, ChatFormatting.GRAY, trust < HOSTILE ? "tardis_controls_frozen" : "tardis_control_sticks");

        TardisDiscovery.reveal(tardis, MindEnums.Reveal.ITS_CONTROLS_STICK);

        if (trust < CAUTIOUS && TardisMinds.calmAround(tardis, player.getUUID())) {
            TardisMinds.addTrust(tardis, player.getUUID(), TRUST_PER_JAM);
        }

        return true;
    }

    private static final float AFRAID_JAM = 0.2f;

    public static float jamChance(float trust) {
        if (trust < HOSTILE) return 1f;

        if (trust < WARY) {
            return Mth.lerp((trust - HOSTILE) / (WARY - HOSTILE), WARY_MAX_JAM, WARY_MIN_JAM);
        }

        if (trust < CAUTIOUS) return CAUTIOUS_JAM;

        return 0f;
    }
}
