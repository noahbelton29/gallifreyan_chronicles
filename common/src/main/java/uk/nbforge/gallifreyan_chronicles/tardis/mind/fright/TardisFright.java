package uk.nbforge.gallifreyan_chronicles.tardis.mind.fright;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisFrightPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.MindMessages;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisBond;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindActions;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;

/**
 * Makes a TARDIS react when a player it fears comes inside.
 * Lights and hum act strangely and the personality may react too.
 * How strong it is depends on what the player did to it, and the fear and trauma built up.
 * It cannot hurt the player and it has a cooldown.
 */
public final class TardisFright {

    private static final long CHECK_TICKS = 10L;

    // Gives players a moment after joining before any check
    private static final int JOIN_GRACE_TICKS = 100;

    // A short wait after entering before it reacts
    private static final int ARRIVAL_DELAY_TICKS = 20;

    // The wait between reactions from the same TARDIS for one player
    private static final long COOLDOWN_TICKS = 1200L;

    // It needs at least this much fear to react
    private static final float MIN_SEVERITY = 0.17f;

    // Below this it is uneasy, not afraid
    private static final float UNEASY_BELOW = 0.35f;

    // Below this it is afraid, above it the TARDIS dreads the player
    private static final float AFRAID_BELOW = 0.65f;

    // Trauma or fear needed for the full effect
    private static final float TRAUMA_FOR_FULL = 60f;
    private static final float FEAR_FOR_FULL = 60f;

    // Hits needed for a full lasting scar
    private static final int HITS_FOR_FULL_SCAR = 20;

    // The most the scars can add to the fear
    private static final float MAX_SCAR = 0.25f;

    // Extra fear if the player betrayed the TARDIS
    private static final float BETRAYED_BONUS = 0.25f;

    // Extra fear if it has PTSD
    private static final float PTSD_BONUS = 0.15f;

    // The shortest and longest the effect lasts
    private static final int MIN_EFFECT_TICKS = 80;
    private static final int MAX_EFFECT_TICKS = 300;

    private static final Map<UUID, UUID> INSIDE = new HashMap<>();

    // When each player last triggered a reaction
    private static final Map<String, Long> LAST = new HashMap<>();

    private TardisFright() {}

    public static void reset() {
        INSIDE.clear();
        LAST.clear();
    }

    /**
     * Fear of one player from 0 to 1.
     * Trauma, fear and hits all add to it, betrayal and PTSD make it worse.
     */
    public static float severity(Tardis tardis, UUID player) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        TardisBond bond = TardisMindSystem.bond(tardis, player);
        // Fear already includes the lasting fear that trauma leaves towards whoever hurt it
        float fear = Mth.clamp(TardisMindSystem.fear(tardis, player) / FEAR_FOR_FULL, 0f, 1f);
        if (fear <= 0f) return 0f;

        // The past only makes current fear worse, once fear is gone so is the reaction
        float scar = Mth.clamp(bond.hits() / (float) HITS_FOR_FULL_SCAR, 0f, 1f) * MAX_SCAR;
        float worse = 1f + scar;
        if (bond.betrayed()) worse += BETRAYED_BONUS;
        worse += PTSD_BONUS * mind.ptsdStrength();
        return Mth.clamp(fear * worse, 0f, 1f);
    }

    /** Reactions are only checked when someone enters a different TARDIS, staying in the same one does not keep triggering them. */
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

    private static void entered(Tardis tardis, ServerPlayer player, long now) {
        if (severity(tardis, player.getUUID()) < MIN_SEVERITY) return;

        String key = tardis.id() + "/" + player.getUUID();
        Long last = LAST.get(key);
        if (last != null && now - last < COOLDOWN_TICKS) return;
        LAST.put(key, now);

        GCSchedulerUtil.runLater(tardis.server(), ARRIVAL_DELAY_TICKS, () -> react(tardis, player));
    }

    /**
     * Checks the player is still inside the same TARDIS first.
     * Then sends the effect to everyone inside, shows the fear to the player and runs the personality enter actions.
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
        TardisFrightPayload payload =
                new TardisFrightPayload(severity, ticks, level.dimension().identifier());
        for (ServerPlayer inside : level.players()) {
            Services.NETWORK.sendTardisFright(inside, payload);
        }
        announce(player, severity);

        BlockPos console = tardis.consolePos().orElse(player.blockPosition());
        MindContext context = new MindContext(tardis, level, player, Vec3.atCenterOf(console));
        context.severity = severity;
        MindActions.run(TardisMindSystem.mind(tardis).personality().onEnter(), context);
    }

    /** Unease, fear or dread depending on how bad it is. */
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
