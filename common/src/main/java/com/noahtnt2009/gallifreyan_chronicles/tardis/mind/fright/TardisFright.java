package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.fright;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.MindMessages;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisFrightPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisBond;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles how a TARDIS reacts to players it has learned to fear.
 * <p>
 * Entering a frightened TARDIS can cause its lights and hum to behave
 * strangely. Depending on its personality, it may also react to the player.
 * <p>
 * How strongly it reacts depends on what the player has done to it and
 * how much fear or trauma it has built up.
 * <p>
 * These reactions cannot hurt the player and have a cooldown.
 */
public final class TardisFright {

    /**
     * How often to check which TARDIS each player is inside.
     */
    private static final long CHECK_TICKS = 10L;

    /**
     * Give players a moment after joining before checking for reactions.
     */
    private static final int JOIN_GRACE_TICKS = 100;

    /**
     * Wait a short moment after entering before the TARDIS reacts.
     */
    private static final int ARRIVAL_DELAY_TICKS = 20;

    /**
     * How long a player must wait before triggering another reaction
     * from the same TARDIS.
     */
    private static final long COOLDOWN_TICKS = 1200L;

    /**
     * The TARDIS needs at least this much fear to react.
     */
    private static final float MIN_SEVERITY = 0.17f;

    /**
     * Below this point, the TARDIS is uneasy rather than frightened.
     */
    private static final float UNEASY_BELOW = 0.35f;

    /**
     * Below this point, the TARDIS is afraid rather than dreading the player.
     */
    private static final float AFRAID_BELOW = 0.65f;

    /**
     * How much trauma or fear is needed to reach its maximum contribution.
     */
    private static final float TRAUMA_FOR_FULL = 60f;
    private static final float FEAR_FOR_FULL = 60f;

    /**
     * How many hits it takes for the player's actions to leave a lasting scar.
     */
    private static final int HITS_FOR_FULL_SCAR = 20;

    /**
     * The most recorded hits can add to the overall fear level.
     */
    private static final float MAX_SCAR = 0.25f;

    /**
     * Adds extra fear if the player has betrayed the TARDIS.
     */
    private static final float BETRAYED_BONUS = 0.25f;

    /**
     * Adds extra fear if the TARDIS has PTSD.
     */
    private static final float PTSD_BONUS = 0.15f;

    /**
     * The shortest and longest the fright effect can last.
     */
    private static final int MIN_EFFECT_TICKS = 80;
    private static final int MAX_EFFECT_TICKS = 300;

    /**
     * Keeps track of which TARDIS each player is currently inside.
     */
    private static final Map<UUID, UUID> INSIDE = new HashMap<>();

    /**
     * Remembers when each player last triggered a reaction from a TARDIS.
     */
    private static final Map<String, Long> LAST = new HashMap<>();

    private TardisFright() {
    }

    public static void reset() {
        INSIDE.clear();
        LAST.clear();
    }

    /**
     * Works out how frightened a TARDIS is of a particular player.
     * <p>
     * Trauma, fear and the number of times the player has hit it all
     * contribute to the result. Betrayal and PTSD make things worse.
     * <p>
     * The final value is kept between 0 and 1, where 0 means no fear
     * and 1 means the TARDIS is as frightened as it can possibly be.
     *
     * @param tardis the TARDIS checking the player
     * @param player the player being checked
     * @return how frightened the TARDIS is of that player
     */
    public static float severity(Tardis tardis, UUID player) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        TardisBond bond = TardisMindSystem.bond(tardis, player);
        /* fear already includes the lasting fear trauma leaves towards whoever hurt it */
        float fear = Mth.clamp(TardisMindSystem.fear(tardis, player) / FEAR_FOR_FULL, 0f, 1f);
        if (fear <= 0f) return 0f;

        /* the past only makes current fear worse. once fear is gone, so is the reaction */
        float scar = Mth.clamp(bond.hits() / (float) HITS_FOR_FULL_SCAR, 0f, 1f) * MAX_SCAR;
        float worse = 1f + scar;
        if (bond.betrayed()) worse += BETRAYED_BONUS;
        worse += PTSD_BONUS * mind.ptsdStrength();
        return Mth.clamp(fear * worse, 0f, 1f);
    }

    /**
     * Checks every so often whether players have entered or left a TARDIS.
     * <p>
     * Reactions are only checked when someone enters a different TARDIS.
     * Staying inside the same one won't keep triggering them.
     *
     * @param server the server containing the players
     */
    public static void tick(MinecraftServer server) {
        long now = TardisMindSystem.now(server);
        if (now % CHECK_TICKS != 0L) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            Tardis inside = TardisApi.ofLevel(player.level()).orElse(null);
            if (inside == null) {
                INSIDE.remove(id);
                continue;
            }

            UUID previous = INSIDE.put(id, inside.id());
            if (inside.id().equals(previous)) continue;
            if (previous == null && player.tickCount < JOIN_GRACE_TICKS) continue;
            if (player.isSpectator()) continue;
            entered(inside, player, now);
        }
    }

    /**
     * Called when a player enters a TARDIS that is frightened of them.
     * <p>
     * Checks how frightened the TARDIS is and makes sure the player
     * isn't still on cooldown before scheduling a reaction.
     *
     * @param tardis the TARDIS the player entered
     * @param player the player who entered
     * @param now    the current game time
     */
    private static void entered(Tardis tardis, ServerPlayer player, long now) {
        if (severity(tardis, player.getUUID()) < MIN_SEVERITY) return;

        String key = tardis.id() + "/" + player.getUUID();
        Long last = LAST.get(key);
        if (last != null && now - last < COOLDOWN_TICKS) return;
        LAST.put(key, now);

        GCScheduler.runLater(tardis.server(), ARRIVAL_DELAY_TICKS, () -> react(tardis, player));
    }

    /**
     * Makes the TARDIS react to the player.
     * <p>
     * First checks that the player is still inside the same TARDIS.
     * It then sends the fright effect to everyone inside, shows the player
     * how frightened the TARDIS is, and runs its personality's onEnter actions.
     *
     * @param tardis the TARDIS reacting
     * @param player the player who triggered the reaction
     */
    private static void react(Tardis tardis, ServerPlayer player) {
        if (player.isRemoved()) return;
        Tardis still = TardisApi.ofLevel(player.level()).orElse(null);
        if (still == null || !still.id().equals(tardis.id())) return;
        if (!(player.level() instanceof ServerLevel)) return;

        ServerLevel level = (ServerLevel) player.level();
        float severity = severity(tardis, player.getUUID());
        if (severity < MIN_SEVERITY) return;

        int ticks = Math.round(Mth.lerp(severity, (float) MIN_EFFECT_TICKS, (float) MAX_EFFECT_TICKS));
        TardisFrightPayload payload = new TardisFrightPayload(severity, ticks, level.dimension().identifier());
        for (ServerPlayer inside : level.players()) {
            Services.NETWORK.sendTardisFright(inside, payload);
        }
        announce(player, severity);

        BlockPos console = tardis.consolePos().orElse(player.blockPosition());
        MindContext context = new MindContext(tardis, level, player, Vec3.atCenterOf(console));
        context.severity = severity;
        MindActions.run(TardisMindSystem.mind(tardis).personality().onEnter(), context);
    }

    /**
     * Shows the player how frightened the TARDIS is through an overlay message.
     * <p>
     * The message changes depending on how severe the fear is:
     * a little unease, outright fear, or complete dread.
     *
     * @param player   the player receiving the message
     * @param severity how frightened the TARDIS currently is
     */
    private static void announce(ServerPlayer player, float severity) {
        if (severity < UNEASY_BELOW) {
            MindMessages.say(player, ChatFormatting.GRAY, "tardis_uneasy");
        } else if (severity < AFRAID_BELOW) {
            MindMessages.say(player, ChatFormatting.GOLD, "tardis_afraid");
        } else {
            MindMessages.say(player, ChatFormatting.RED, "tardis_dreads_you");
        }
    }
}
