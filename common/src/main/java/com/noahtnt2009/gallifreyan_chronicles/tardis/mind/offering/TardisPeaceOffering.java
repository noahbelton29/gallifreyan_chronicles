package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.offering;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisBond;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/* a gift to make up with a tardis. it helps, but it can't replace earning trust:
   it stops working once the tardis is fond of you, it needs a long gap between gifts, repeat gifts
   count for less, and a frightened or betrayed tardis won't take it at all */
public final class TardisPeaceOffering {
    private static final float TRUST_GAIN = 6f;
    private static final float STRESS_RELIEF = 10f;
    private static final float FEAR_RELIEF = 15f;
    private static final float TRAUMA_RELIEF = 1f;
    private static final float TRUST_CEILING = 60f;
    private static final float TOO_AFRAID = 40f;
    private static final float PTSD_EFFECT = 0.5f;
    private static final float STREAK_FALLOFF = 0.25f;
    private static final float MIN_EFFECT = 0.25f;
    private static final long COOLDOWN_TICKS = 6000L;
    private static final long STREAK_WINDOW_TICKS = 24000L;

    private static final Map<String, Offering> OFFERINGS = new HashMap<>();

    private static final class Offering {
        final long tick;
        final int streak;

        Offering(long tick, int streak) {
            this.tick = tick;
            this.streak = streak;
        }
    }

    private TardisPeaceOffering() {
    }

    public static void reset() {
        OFFERINGS.clear();
    }

    /* true if the tardis took it, so the gift should be used up */
    public static boolean offer(Tardis tardis, ServerLevel level, BlockPos pos, Player player) {
        UUID who = player.getUUID();
        TardisMind mind = TardisMindSystem.mind(tardis);
        TardisBond bond = TardisMindSystem.bond(tardis, who);

        if (bond.betrayed()) {
            Messages.overlay(player, ChatFormatting.DARK_RED, "tardis_offering_betrayed");
            return false;
        }
        if (TardisMindSystem.fear(tardis, who) >= TOO_AFRAID) {
            Messages.overlay(player, ChatFormatting.DARK_GRAY, "tardis_offering_afraid");
            return false;
        }
        if (bond.trust() >= TRUST_CEILING - 1f) {
            Messages.overlay(player, ChatFormatting.GRAY, "tardis_offering_content");
            return false;
        }

        long now = TardisMindSystem.now(tardis.server());
        String key = tardis.id() + "/" + who;
        Offering last = OFFERINGS.get(key);
        if (last != null && now - last.tick < COOLDOWN_TICKS) {
            Messages.overlay(player, ChatFormatting.GRAY, "tardis_offering_wait");
            return false;
        }

        int streak = last != null && now - last.tick < STREAK_WINDOW_TICKS ? last.streak + 1 : 0;
        float effect = Math.max(MIN_EFFECT, 1f - STREAK_FALLOFF * streak);
        boolean ptsd = mind.has(TardisDisability.PTSD);
        if (ptsd) effect *= PTSD_EFFECT;

        /* trust traits scale the gain, so work out how much to give to land exactly on the ceiling */
        float scale = Math.max(0.01f, mind.modifiersFor(bond).trustGain());
        float room = (TRUST_CEILING - bond.trust()) / scale;
        float gain = Math.min(TRUST_GAIN * effect, room);

        TardisMindSystem.addTrust(tardis, who, gain);
        TardisMindSystem.addStress(tardis, -STRESS_RELIEF * effect);
        TardisMindSystem.addFear(tardis, who, -FEAR_RELIEF * effect);
        TardisMindSystem.setTrauma(tardis, Math.max(0f, TardisMindSystem.trauma(tardis) - TRAUMA_RELIEF * effect));
        OFFERINGS.put(key, new Offering(now, streak));

        level.playSound(null, pos, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.BLOCKS, 0.8F, 1.2F);
        String message = ptsd ? "tardis_offering_nervous" : streak > 0 ? "tardis_offering_again" : "tardis_offering_accepted";
        Messages.overlay(player, ChatFormatting.LIGHT_PURPLE, message);
        return true;
    }
}
