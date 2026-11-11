package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisTerror;
import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/* low trust makes the controls stick. keep trying and it slowly gives in */
public final class TardisWariness {
    public static final float HOSTILE = 5f;
    public static final float WARY = 20f;
    public static final float CAUTIOUS = 35f;
    private static final float WARY_MAX_JAM = 0.6f;
    private static final float WARY_MIN_JAM = 0.25f;
    private static final float CAUTIOUS_JAM = 0.1f;
    private static final float TRUST_PER_JAM = 1f;

    private TardisWariness() {
    }

    /* true if the control refuses to move */
    public static boolean jams(TardisControlEntity control, TardisConsoleBlockEntity console, Player player) {
        if (!(control.level() instanceof ServerLevel level)) return false;
        Tardis tardis = TardisApi.of(console).orElse(null);
        if (tardis == null) return false;

        if (TardisTerror.compliant(tardis)) return false;
        if (TardisTerror.terrified(tardis)) {
            level.playSound(null, control.blockPosition(), GCSounds.LEVER, SoundSource.BLOCKS, 0.5F, 0.4F);
            Messages.overlay(player, ChatFormatting.DARK_GRAY, "tardis_terrified");
            return true;
        }

        float trust = TardisMindSystem.trust(tardis);
        if (level.getRandom().nextFloat() >= jamChance(trust)) return false;

        level.playSound(null, control.blockPosition(), GCSounds.LEVER, SoundSource.BLOCKS, 0.7F, 0.5F);
        level.sendParticles(ParticleTypes.SMOKE, control.getX(), control.getY() + 0.1, control.getZ(), 4, 0.05, 0.05, 0.05, 0.01);
        Messages.overlay(player, ChatFormatting.GRAY, trust < HOSTILE ? "tardis_controls_frozen" : "tardis_control_sticks");

        TardisDiscovery.trait(tardis, TardisTrait.DISTRUSTING);
        if (trust < CAUTIOUS) TardisMindSystem.addTrust(tardis, TRUST_PER_JAM);
        return true;
    }

    public static float jamChance(float trust) {
        if (trust < HOSTILE) return 1f;
        if (trust < WARY) return Mth.lerp((trust - HOSTILE) / (WARY - HOSTILE), WARY_MAX_JAM, WARY_MIN_JAM);
        if (trust < CAUTIOUS) return CAUTIOUS_JAM;
        return 0f;
    }
}
