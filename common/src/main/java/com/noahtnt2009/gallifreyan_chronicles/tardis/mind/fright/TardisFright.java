package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.fright;

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
import com.noahtnt2009.gallifreyan_chronicles.util.Messages;
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

/* a tardis you've hurt is afraid of you. step inside and its hum warps and its lights stutter, and
   depending on its personality it may lash out. how bad it is depends on how badly you treated it.
   lashing out never kills: it always leaves you at least a little health */
public final class TardisFright {
    private static final long CHECK_TICKS = 10L;
    private static final int JOIN_GRACE_TICKS = 100;
    private static final int ARRIVAL_DELAY_TICKS = 20;
    private static final long COOLDOWN_TICKS = 1200L;

    private static final float MIN_SEVERITY = 0.12f;
    private static final float UNEASY_BELOW = 0.35f;
    private static final float AFRAID_BELOW = 0.65f;

    private static final float TRAUMA_FOR_FULL = 60f;
    private static final float FEAR_FOR_FULL = 60f;
    private static final int HITS_FOR_FULL_SCAR = 20;
    private static final float MAX_SCAR = 0.25f;
    private static final float BETRAYED_BONUS = 0.25f;
    private static final float PTSD_BONUS = 0.15f;

    private static final int MIN_EFFECT_TICKS = 80;
    private static final int MAX_EFFECT_TICKS = 300;

    private static final Map<UUID, UUID> INSIDE = new HashMap<>();
    private static final Map<String, Long> LAST = new HashMap<>();

    private TardisFright() {
    }

    public static void reset() {
        INSIDE.clear();
        LAST.clear();
    }

    /* 0 for someone who never hurt it, up to 1 for someone it is terrified of */
    public static float severity(Tardis tardis, UUID player) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        TardisBond bond = TardisMindSystem.bond(tardis, player);
        boolean hurt = bond.hurtIt() || bond.betrayed();
        float fear = Mth.clamp(TardisMindSystem.fear(tardis, player) / FEAR_FOR_FULL, 0f, 1f);
        if (!hurt && fear <= 0f) return 0f;

        float trauma = hurt ? Mth.clamp(mind.trauma() / TRAUMA_FOR_FULL, 0f, 1f) : 0f;
        float scar = Mth.clamp(bond.hits() / (float) HITS_FOR_FULL_SCAR, 0f, 1f) * MAX_SCAR;

        float severity = Math.max(Math.max(trauma, fear), scar);
        if (bond.betrayed()) severity += BETRAYED_BONUS;
        if (mind.has(TardisDisability.PTSD)) severity += PTSD_BONUS;
        return Mth.clamp(severity, 0f, 1f);
    }

    /* called every server tick. cheap: it only looks every half second */
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

        GCScheduler.runLater(tardis.server(), ARRIVAL_DELAY_TICKS, () -> react(tardis, player));
    }

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

    private static void announce(ServerPlayer player, float severity) {
        if (severity < UNEASY_BELOW) {
            Messages.overlay(player, ChatFormatting.GRAY, "tardis_uneasy");
        } else if (severity < AFRAID_BELOW) {
            Messages.overlay(player, ChatFormatting.GOLD, "tardis_afraid");
        } else {
            Messages.overlay(player, ChatFormatting.RED, "tardis_dreads_you");
        }
    }
}
