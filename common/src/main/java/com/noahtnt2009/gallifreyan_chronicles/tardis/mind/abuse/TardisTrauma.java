package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.FearLevel;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.MindMessages;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisBond;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood.TardisMoods;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/* long term scars from abuse. each player leaves their own scars, and the tardis also keeps a
   total. enough total trauma gives it ptsd. all of it heals with time and kindness */
public final class TardisTrauma {
    private static final float PTSD_TRAUMA = 80f;
    private static final float MIN_SENSITIVITY = 0.75f;
    private static final float MAX_SENSITIVITY = 1.25f;

    /* the same beating does less lasting harm with every extra hit */
    private static final float BEATING_FALLOFF = 0.25f;
    private static final float BEATING_MIN = 0.25f;

    private static final float CALM_STRESS = 30f;
    private static final float CALM_LANDING_HEAL = 1.5f;
    /* each calm landing gets it this much closer to recovering from ptsd */
    private static final float RECOVERY_KEEP = 0.97f;
    private static final float EASING_RECOVERY = 0.9f;

    private static final long REACT_COOLDOWN_TICKS = 100L;
    private static final double INSIDE_RANGE = 10.0;
    private static final float SIGHT_FEAR = 2f;
    private static final float SIGHT_STRESS = 2f;

    private static final Map<String, Long> LAST_REACTION = new HashMap<>();

    private TardisTrauma() {
    }

    public static void init() {
        TardisEvents.LAND.register(TardisTrauma::heal);
    }

    public static void reset() {
        LAST_REACTION.clear();
    }

    /* how much one hit scars it. loyal ones feel it the most. hitInBeating counts from 1 */
    public static void hit(Tardis tardis, UUID player, int hitInBeating) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        TardisBond bond = TardisMindSystem.bond(tardis, player);
        float falloff = Math.max(BEATING_MIN, 1f - BEATING_FALLOFF * (hitInBeating - 1));
        float hurt = mind.personality().traumaPerHit() * sensitivity(tardis) * mind.modifiersFor(bond).traumaGain() * falloff;

        TardisMindSystem.setTraumaFrom(tardis, player, TardisMindSystem.traumaFrom(tardis, player) + hurt);
        float total = TardisMindSystem.trauma(tardis) + hurt;
        TardisMindSystem.setTrauma(tardis, total);

        if (total >= PTSD_TRAUMA && !mind.has(TardisDisability.PTSD)) {
            List<TardisDisability> disabilities = new ArrayList<>(mind.disabilities());
            disabilities.add(TardisDisability.PTSD);
            TardisMindSystem.setDisabilities(tardis, disabilities);
            TardisMindSystem.setRecovery(tardis, 0f);
            TardisDiscovery.disability(tardis, TardisDisability.PTSD);
        }
    }

    /* every tardis takes it differently, but always the same for the same tardis */
    private static float sensitivity(Tardis tardis) {
        UUID id = tardis.id();
        RandomSource random = RandomSource.create(id.getMostSignificantBits() ^ id.getLeastSignificantBits() ^ 0x7A5E1L);
        return Mth.lerp(random.nextFloat(), MIN_SENSITIVITY, MAX_SENSITIVITY);
    }

    /* a calm landing heals a little: its trauma, the crew's scars and hits, and its ptsd */
    private static void heal(Tardis tardis) {
        if (TardisMindSystem.stress(tardis) >= CALM_STRESS) return;

        float trauma = TardisMindSystem.trauma(tardis);
        if (trauma > 0f) TardisMindSystem.setTrauma(tardis, Math.max(0f, trauma - CALM_LANDING_HEAL));

        for (UUID player : TardisMoods.crew(tardis)) {
            if (TardisMindSystem.bond(tardis, player).hurtIt() || TardisMindSystem.traumaFrom(tardis, player) > 0f) {
                TardisMindSystem.forgiveOneHit(tardis, player, CALM_LANDING_HEAL);
            }
        }

        TardisMind mind = TardisMindSystem.mind(tardis);
        if (!mind.has(TardisDisability.PTSD)) return;
        float before = mind.recovery();
        float after = 1f - (1f - before) * RECOVERY_KEEP;
        TardisMindSystem.setRecovery(tardis, after);
        if (before < EASING_RECOVERY && after >= EASING_RECOVERY) {
            tardis.ownerPlayer().ifPresent(owner -> MindMessages.say(owner, ChatFormatting.LIGHT_PURPLE, "tardis_ptsd_easing"));
        }
    }

    /* someone it's afraid of is holding a mallet nearby */
    public static void sawMallet(Player player, ServerLevel level) {
        for (Tardis tardis : nearby(player, level)) {
            UUID who = player.getUUID();
            if (!TardisMindSystem.fearLevel(tardis, who).atLeast(FearLevel.AFRAID)) continue;

            long now = TardisMindSystem.now(tardis.server());
            String key = tardis.id() + "/" + who;
            Long last = LAST_REACTION.get(key);
            if (last != null && now - last < REACT_COOLDOWN_TICKS) continue;
            LAST_REACTION.put(key, now);

            TardisMindSystem.addFear(tardis, who, SIGHT_FEAR);
            TardisMindSystem.addStress(tardis, SIGHT_STRESS);
            BlockPos at = reactionPos(tardis, level);
            if (at != null) react(tardis, TardisMindSystem.mind(tardis).personality(), player, level, at);
        }
    }

    private static void react(Tardis tardis, TardisPersonality personality, Player player, ServerLevel level, BlockPos at) {
        MindActions.run(personality.onMalletSeen(), new MindContext(tardis, level, player, Vec3.atCenterOf(at)));
        TardisDiscovery.reveal(tardis, Reveal.IT_SEES_THE_MALLET);
    }

    /* only tardises in this dimension, so it doesn't scan every tardis on the server */
    private static List<Tardis> nearby(Player player, ServerLevel level) {
        /* only notices the mallet from inside, not from the box outside */
        List<Tardis> found = new ArrayList<>();
        TardisApi.ofLevel(level).ifPresent(inside -> inside.consolePos().ifPresent(pos -> {
            if (player.distanceToSqr(Vec3.atCenterOf(pos)) <= INSIDE_RANGE * INSIDE_RANGE) found.add(inside);
        }));
        return found;
    }

    private static BlockPos reactionPos(Tardis tardis, ServerLevel level) {
        if (level == tardis.interiorLevel().orElse(null)) return tardis.consolePos().orElse(null);
        return tardis.exteriorPos().orElse(null);
    }
}
