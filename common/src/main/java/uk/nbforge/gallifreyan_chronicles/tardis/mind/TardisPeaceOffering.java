package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerState;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;

public final class TardisPeaceOffering {
    private static final float TRUST_GAIN = 6f;
    private static final float STRESS_RELIEF = 10f;
    private static final float FEAR_RELIEF = 15f;
    private static final float TRAUMA_RELIEF = 1f;

    private static final float TRUST_CEILING = 70f;

    private static final float WARY_EFFECT = 0.5f;

    private static final float PTSD_EFFECT = 0.5f;

    private static final float STREAK_FALLOFF = 0.25f;

    private static final float MIN_EFFECT = 0.25f;

    private static final long COOLDOWN_TICKS = 6000L;

    private static final long STREAK_WINDOW_TICKS = 24000L;

    private static final Map<String, Offering> OFFERINGS = ServerState.track(new HashMap<>());

    /** when it was given and how many in a row */
    private static final class Offering {
        final long tick;
        final int streak;

        Offering(long tick, int streak) {
            this.tick = tick;
            this.streak = streak;
        }
    }

    private TardisPeaceOffering() {}

    public static void reset() {
        OFFERINGS.clear();
    }

    public static boolean offer(Tardis tardis, ServerLevel level, BlockPos pos, Player player) {
        UUID who = player.getUUID();
        TardisMind mind = TardisMinds.mind(tardis);
        TardisBond bond = TardisMinds.bond(tardis, who);

        if (bond.trust() >= TRUST_CEILING - 1f) {
            MindMessages.say(player, ChatFormatting.GRAY, "tardis_offering_content");
            return false;
        }

        long now = TardisMinds.now(tardis.server());
        String key = tardis.id() + "/" + who;
        Offering last = OFFERINGS.get(key);
        if (last != null && now - last.tick < COOLDOWN_TICKS) {
            MindMessages.say(player, ChatFormatting.GRAY, "tardis_offering_wait");
            return false;
        }

        int streak = last != null && now - last.tick < STREAK_WINDOW_TICKS ? last.streak + 1 : 0;
        float effect = Math.max(MIN_EFFECT, 1f - STREAK_FALLOFF * streak);

        boolean ptsd = mind.has(TardisDisability.PTSD);
        if (ptsd) effect *= 1f - (1f - PTSD_EFFECT) * mind.ptsdStrength();
        boolean betrayed = bond.betrayed();
        boolean afraid = TardisMinds.fearLevel(tardis, who).atLeast(FearLevel.AFRAID);
        if (betrayed || afraid) effect *= WARY_EFFECT;

        float scale = Math.max(0.01f, mind.modifiersFor(bond).trustGain());
        float room = (TRUST_CEILING - bond.trust()) / scale;
        float gain = Math.min(TRUST_GAIN * effect, room);

        TardisMinds.addTrust(tardis, who, gain);
        TardisMinds.addStress(tardis, -STRESS_RELIEF * effect);
        TardisMinds.addFear(tardis, who, -FEAR_RELIEF * effect);
        TardisMinds.setTrauma(tardis, Math.max(0f, TardisMinds.trauma(tardis) - TRAUMA_RELIEF * effect));
        TardisMinds.forgiveOneHit(tardis, who, TRAUMA_RELIEF);
        OFFERINGS.put(key, new Offering(now, streak));

        level.playSound(null, pos, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.BLOCKS, 0.8f, 1.2f);
        String message = betrayed
                ? "tardis_offering_betrayed"
                : afraid
                        ? "tardis_offering_afraid"
                        : ptsd
                                ? "tardis_offering_nervous"
                                : streak > 0 ? "tardis_offering_again" : "tardis_offering_accepted";
        MindMessages.say(player, ChatFormatting.LIGHT_PURPLE, message);
        return true;
    }
}
