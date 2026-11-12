package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;

import com.noahtnt2009.gallifreyan_chronicles.network.DiscoveryToastPayload;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/* tardises keep their mind to themselves until they show it */
public final class TardisDiscovery {
    private static final String ROOT = "tardis_mind/root";
    private static final float NOTICEABLE = 5f;
    private static final float OBEDIENT_REVEAL_STRESS = 50f;
    private static final double NEARBY_RANGE = 24.0;

    private TardisDiscovery() {
    }

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisDiscovery::onTakeOff);
        TardisEvents.LAND.register(TardisDiscovery::onLand);
    }

    /* obedient ones show it by flying anyway when they're properly stressed */
    private static void onTakeOff(Tardis tardis) {
        if (TardisMindSystem.mind(tardis).personality().refusesToFly()) return;
        if (TardisMindSystem.stress(tardis) >= OBEDIENT_REVEAL_STRESS) personality(tardis);
    }

    private static void onLand(Tardis tardis) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        boolean upset = mind.stress() >= NOTICEABLE || mind.trauma() >= NOTICEABLE;
        if (!upset) return;
        reveal(tardis, Reveal.IT_LANDS_UPSET);
    }

    public static void personality(Tardis tardis) {
        String id = TardisMindSystem.mind(tardis).personality().id();
        reveal(tardis, "personality", id, "personality", "tardis_mind/personality");
    }

    public static void reveal(Tardis tardis, Reveal trigger) {
        for (TardisTrait trait : TardisMindSystem.mind(tardis).traits()) {
            if (trait.reveal() == trigger) trait(tardis, trait);
        }
    }

    public static void trait(Tardis tardis, TardisTrait trait) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (!mind.has(trait)) return;
        if (mind.knows("trait:" + trait.localId())) return;
        reveal(tardis, "trait:" + trait.id(), trait.id(), "trait", "tardis_mind/trait");
    }

    public static void disability(Tardis tardis, TardisDisability disability) {
        if (!TardisMindSystem.mind(tardis).has(disability)) return;
        reveal(tardis, "disability:" + disability.id(), disability.id(), "disability", "tardis_mind/" + disability.id());
    }

    /* only counts as discovered if the owner is there to see it */
    private static void reveal(Tardis tardis, String key, String id, String kind, String advancement) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (mind.knows(key)) return;
        List<ServerPlayer> audience = audience(tardis, kind);
        if (audience.isEmpty()) return;

        List<String> discovered = new ArrayList<>(mind.discovered());
        discovered.add(key);
        TardisMindSystem.setDiscovered(tardis, discovered);

        for (ServerPlayer player : audience) {
            DiscoveryToastPayload.send(player, kind, id);
            award(player, ROOT);
            award(player, advancement);
        }
    }

    private static List<ServerPlayer> audience(Tardis tardis, String kind) {
        ServerPlayer owner = tardis.ownerPlayer().orElse(null);
        if (owner == null) return List.of();
        if (!"disability".equals(kind) && !isThere(tardis, owner)) return List.of();
        return List.of(owner);
    }

    private static boolean isThere(Tardis tardis, ServerPlayer owner) {
        if (owner.level() == tardis.interiorLevel().orElse(null)) return true;
        ServerLevel outside = tardis.exteriorLevel().orElse(null);
        BlockPos box = tardis.exteriorPos().orElse(null);
        return outside != null && box != null && owner.level() == outside
                && owner.distanceToSqr(Vec3.atCenterOf(box)) <= NEARBY_RANGE * NEARBY_RANGE;
    }

    private static void award(ServerPlayer player, String path) {
        MinecraftServer server = player.level().getServer();
        AdvancementHolder holder = server.getAdvancements().get(GCUtils.modIdentifier(path));
        if (holder != null) player.getAdvancements().award(holder, "discovered");
    }
}
