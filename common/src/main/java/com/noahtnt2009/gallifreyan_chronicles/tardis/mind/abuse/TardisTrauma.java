package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisBond;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/* long term scars from abuse. enough of it and the tardis gets ptsd */
public final class TardisTrauma {
    private static final float PTSD_TRAUMA = 60f;
    private static final float MIN_SENSITIVITY = 0.75f;
    private static final float MAX_SENSITIVITY = 1.25f;
    private static final float CALM_STRESS = 30f;
    private static final float CALM_LANDING_HEAL = 1.5f;

    private static final float WARY_TRAUMA = 10f;
    private static final long REACT_COOLDOWN_TICKS = 100L;
    private static final double INSIDE_RANGE = 10.0;
    private static final double OUTSIDE_RANGE = 8.0;
    private static final float SIGHT_FEAR = 2f;
    private static final float SIGHT_FEAR_PER_TRAUMA = 0.05f;
    private static final float SIGHT_STRESS = 2f;

    private static final Map<UUID, Long> LAST_REACTION = new HashMap<>();

    private TardisTrauma() {
    }

    public static void init() {
        TardisEvents.LAND.register(TardisTrauma::heal);
    }

    public static void reset() {
        LAST_REACTION.clear();
    }

    /* how much one hit scars it. loyal ones feel it the most */
    public static void hit(Tardis tardis, UUID player) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        TardisBond bond = TardisMindSystem.bond(tardis, player);
        float hurt = mind.personality().traumaPerHit() * sensitivity(tardis) * mind.modifiersFor(bond).traumaGain();
        float trauma = mind.trauma() + hurt;
        TardisMindSystem.setTrauma(tardis, trauma);

        if (trauma >= PTSD_TRAUMA && !mind.has(TardisDisability.PTSD)) {
            List<TardisDisability> disabilities = new ArrayList<>(mind.disabilities());
            disabilities.add(TardisDisability.PTSD);
            TardisMindSystem.setDisabilities(tardis, disabilities);
            TardisDiscovery.disability(tardis, TardisDisability.PTSD);
        }
    }


    /* every tardis takes it differently, but always the same for the same tardis */
    private static float sensitivity(Tardis tardis) {
        UUID id = tardis.id();
        RandomSource random = RandomSource.create(id.getMostSignificantBits() ^ id.getLeastSignificantBits() ^ 0x7A5E1L);
        return Mth.lerp(random.nextFloat(), MIN_SENSITIVITY, MAX_SENSITIVITY);
    }

    private static void heal(Tardis tardis) {
        if (TardisMindSystem.stress(tardis) >= CALM_STRESS) return;
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (mind.trauma() > 0f) TardisMindSystem.setTrauma(tardis, mind.trauma() - CALM_LANDING_HEAL);
    }

    /* someone is holding a mallet nearby */
    public static void sawMallet(Player player, ServerLevel level) {
        for (Tardis tardis : nearby(player, level)) {
            TardisMind mind = TardisMindSystem.mind(tardis);
            if (!TardisMindSystem.hasHurt(tardis, player.getUUID())) continue;
            if (mind.trauma() < WARY_TRAUMA && !mind.has(TardisDisability.PTSD)) continue;

            long now = TardisMindSystem.now(tardis.server());
            Long last = LAST_REACTION.get(tardis.id());
            if (last != null && now - last < REACT_COOLDOWN_TICKS) continue;
            LAST_REACTION.put(tardis.id(), now);

            TardisMindSystem.addFear(tardis, player.getUUID(), SIGHT_FEAR + mind.trauma() * SIGHT_FEAR_PER_TRAUMA);
            TardisMindSystem.addStress(tardis, SIGHT_STRESS);
            BlockPos at = reactionPos(tardis, level);
            if (at != null) react(tardis, mind.personality(), player, level, at);
        }
    }

    private static void react(Tardis tardis, TardisPersonality personality, Player player, ServerLevel level, BlockPos at) {
        MindActions.run(personality.onMalletSeen(), new MindContext(tardis, level, player, Vec3.atCenterOf(at)));
    }

    private static List<Tardis> nearby(Player player, ServerLevel level) {
        List<Tardis> found = new ArrayList<>();
        TardisApi.ofLevel(level).ifPresent(inside -> inside.consolePos().ifPresent(pos -> {
            if (player.distanceToSqr(Vec3.atCenterOf(pos)) <= INSIDE_RANGE * INSIDE_RANGE) found.add(inside);
        }));
        for (Tardis tardis : TardisApi.all(level.getServer())) {
            if (tardis.exteriorLevel().orElse(null) != level) continue;
            tardis.exteriorPos().ifPresent(pos -> {
                if (player.distanceToSqr(Vec3.atCenterOf(pos)) <= OUTSIDE_RANGE * OUTSIDE_RANGE) found.add(tardis);
            });
        }
        return found;
    }

    private static BlockPos reactionPos(Tardis tardis, ServerLevel level) {
        if (level == tardis.interiorLevel().orElse(null)) return tardis.consolePos().orElse(null);
        return tardis.exteriorPos().orElse(null);
    }
}
