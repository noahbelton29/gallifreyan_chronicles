package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;

import com.noahtnt2009.gallifreyan_chronicles.network.TardisAlarmPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisDrumShakePayload;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
import com.noahtnt2009.gallifreyan_chronicles.util.PlayerMotion;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TardisMallet {
    private static final float STRESS_PER_HIT = 8f;
    private static final float TRUST_LOSS_PER_HIT = 6f;
    private static final float SHUDDER_SHAKE = 0.4F;
    private static final float FLINCH_FEAR = 30f;
    private static final float COWER_FEAR = 60f;
    private static final float BASE_BLAST = 0.6F;
    private static final float BLAST_PER_FEAR = 0.02F;

    private static final int ALARM_HITS = 2;
    private static final int BETRAYAL_HITS = 4;
    private static final int LONG_ALARM_HITS = 5;
    private static final int LONG_ALARM_FLASHES = 3;
    private static final long HITS_FORGOTTEN_TICKS = 600L;
    private static final Map<UUID, Hits> HITS = new HashMap<>();

    private record Hits(int count, long lastTick) {
    }

    private TardisMallet() {
    }

    public static void reset() {
        HITS.clear();
    }

    public static void strike(Tardis tardis, ServerLevel level, BlockPos hitPos, Player player) {
        TardisPersonality personality = TardisMindSystem.mind(tardis).personality();
        UUID hitter = player.getUUID();
        TardisMindSystem.addFear(tardis, hitter, personality.malletFear().pick(level.getRandom()));
        TardisMindSystem.addStress(tardis, STRESS_PER_HIT);
        TardisMindSystem.abuse(tardis, hitter, TRUST_LOSS_PER_HIT);
        TardisTrauma.hit(tardis, hitter);

        Vec3 hit = Vec3.atCenterOf(hitPos);
        float blast = BASE_BLAST + TardisMindSystem.fear(tardis, hitter) * BLAST_PER_FEAR;
        level.explode(null, hit.x, hit.y, hit.z, blast, Level.ExplosionInteraction.NONE);
        level.playSound(null, hitPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.6F,
                0.7F + level.getRandom().nextFloat() * 0.2F);
        level.sendParticles(ParticleTypes.CRIT, hit.x, hit.y, hit.z, 12, 0.3, 0.3, 0.3, 0.2);
        TardisDrumShakePayload.broadcast(level, hit, SHUDDER_SHAKE);
        shudderInside(tardis);
        int hits = countHit(tardis);
        alarm(tardis, level, hitPos, player, hits);
        if (hits >= BETRAYAL_HITS && TardisMindSystem.betray(tardis, hitter)) {
            Messages.overlay(player, ChatFormatting.DARK_RED, "tardis_betrayed");
            return;
        }

        if (TardisTerror.strike(tardis, level, hit, player)) return;

        MindContext context = new MindContext(tardis, level, player, hit);
        MindActions.run(personality.onMalletHit(), context);
        if (context.handled) return;
        Messages.overlay(player, ChatFormatting.DARK_GRAY, reaction(TardisMindSystem.fear(tardis, hitter)));
    }



    private static int countHit(Tardis tardis) {
        long now = TardisMindSystem.now(tardis.server());
        Hits last = HITS.get(tardis.id());
        int count = last == null || now - last.lastTick() > HITS_FORGOTTEN_TICKS ? 1 : last.count() + 1;
        HITS.put(tardis.id(), new Hits(count, now));
        return count;
    }

    /* after a couple of hits the cloister bell rings and the lights go red */
    private static void alarm(Tardis tardis, ServerLevel level, BlockPos hitPos, Player player, int hits) {
        if (hits < ALARM_HITS) return;
        int flashes = hits >= LONG_ALARM_HITS ? LONG_ALARM_FLASHES : 1;

        ServerLevel interior = tardis.interiorLevel().orElse(null);
        BlockPos console = tardis.consolePos().orElse(null);
        if (interior != null && console != null) {
            TardisAlarm.flash(interior, console, flashes);
        }
        if (level != interior) {
            if (player instanceof ServerPlayer serverPlayer) {
                TardisAlarmPayload.sendTo(serverPlayer, flashes);
            }
        }
    }

    private static void shudderInside(Tardis tardis) {
        tardis.console().ifPresent(console -> {
            if (console.getLevel() instanceof ServerLevel interior) {
                TardisDrumShakePayload.broadcast(interior, Vec3.atCenterOf(console.getBlockPos()), SHUDDER_SHAKE);
            }
        });
    }

    private static String reaction(float fear) {
        if (fear >= COWER_FEAR) return "mallet_cowers";
        if (fear >= FLINCH_FEAR) return "mallet_flinches";
        return "mallet_shudders";
    }
}
