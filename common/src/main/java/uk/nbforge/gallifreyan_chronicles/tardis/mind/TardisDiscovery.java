package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.network.DiscoveryToastPayload;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

/** Discoveries only count when the owner is around to see them. */
public final class TardisDiscovery {

    private static final String ROOT = "tardis_mind/root";

    private static final float NOTICEABLE = 5f;

    private static final float OBEDIENT_REVEAL_STRESS = 50f;

    private static final double NEARBY_RANGE = 24.0;

    private TardisDiscovery() {}

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisDiscovery::onTakeOff);
        TardisEvents.LAND.register(TardisDiscovery::onLand);
    }

    /**
     * A TARDIS that normally refuses to fly does not reveal its personality just by taking off.
     * If it is obedient and stressed enough, taking off can reveal something.
     */
    private static void onTakeOff(Tardis tardis) {
        if (TardisMinds.mind(tardis).personality().refusesToFly()) return;
        if (TardisMinds.stress(tardis) >= OBEDIENT_REVEAL_STRESS) personality(tardis);
    }

    private static void onLand(Tardis tardis) {
        TardisMind mind = TardisMinds.mind(tardis);
        boolean upset = mind.stress() >= NOTICEABLE || TardisMinds.trauma(tardis) >= NOTICEABLE;
        if (!upset) return;
        reveal(tardis, Reveal.IT_LANDS_UPSET);
    }

    public static void personality(Tardis tardis) {
        String id = TardisMinds.mind(tardis).personality().id();
        reveal(tardis, "personality", id, "personality", "tardis_mind/personality");
    }

    public static void reveal(Tardis tardis, Reveal trigger) {
        for (TardisTrait trait : TardisMinds.mind(tardis).traits()) {
            if (trait.reveal() == trigger) trait(tardis, trait);
        }
    }

    public static void trait(Tardis tardis, TardisTrait trait) {
        TardisMind mind = TardisMinds.mind(tardis);
        if (!mind.has(trait)) return;
        if (mind.knows("trait:" + trait.localId())) return;
        reveal(tardis, "trait:" + trait.id(), trait.id(), "trait", "tardis_mind/trait");
    }

    public static void disability(Tardis tardis, TardisDisability disability) {
        if (!TardisMinds.mind(tardis).has(disability)) return;
        reveal(
                tardis,
                "disability:" + disability.id(),
                disability.id(),
                "disability",
                "tardis_mind/" + disability.id());
    }

    /**
     * The owner has to be there to see it, inside the TARDIS or close to the exterior.
     * Disabilities are different and do not need the owner nearby.
     * Something that is already discovered is not revealed again.
     */
    private static void reveal(Tardis tardis, String key, String id, String kind, String advancement) {
        TardisMind mind = TardisMinds.mind(tardis);
        if (mind.knows(key)) return;
        List<ServerPlayer> audience = audience(tardis, kind);
        if (audience.isEmpty()) return;

        List<String> discovered = new ArrayList<>(mind.discovered());
        discovered.add(key);
        TardisMinds.setDiscovered(tardis, discovered);

        for (ServerPlayer player : audience) {
            DiscoveryToastPayload.send(player, kind, id);
            award(player, ROOT);
            award(player, advancement);
        }
    }

    private static List<ServerPlayer> audience(Tardis tardis, String kind) {
        ServerPlayer owner = tardis.ownerPlayer().orElse(null);
        if (owner == null) return List.of();
        if (!"disability".equals(kind) && isAway(tardis, owner)) return List.of();
        return List.of(owner);
    }

    private static boolean isAway(Tardis tardis, ServerPlayer owner) {
        if (owner.level() == tardis.interiorLevel().orElse(null)) return false;
        ServerLevel outside = tardis.exteriorLevel().orElse(null);
        BlockPos box = tardis.exteriorPos().orElse(null);
        return outside == null
                || box == null
                || owner.level() != outside
                || owner.distanceToSqr(Vec3.atCenterOf(box)) > NEARBY_RANGE * NEARBY_RANGE;
    }

    private static void award(ServerPlayer player, String path) {
        MinecraftServer server = player.level().getServer();
        AdvancementHolder holder = server.getAdvancements().get(GCUtil.modIdentifier(path));
        if (holder != null) player.getAdvancements().award(holder, "discovered");
    }
}
