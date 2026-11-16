package uk.nbforge.gallifreyan_chronicles.tardis.mind.discovery;

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
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

/**
 * Handles players finding out things about a TARDIS.
 * It does not reveal everything at once, players learn the personality, traits and disabilities through events.
 * Discoveries only count when the owner is around to see them.
 */
public final class TardisDiscovery {

    // The advancement unlocked when the mind is first discovered
    private static final String ROOT = "tardis_mind/root";

    // How much stress or trauma it needs before it shows it is upset
    private static final float NOTICEABLE = 5f;

    // How stressed an obedient TARDIS must be before taking off reveals its personality
    private static final float OBEDIENT_REVEAL_STRESS = 50f;

    // How close the owner has to be to the exterior to see a discovery
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
        if (TardisMindSystem.mind(tardis).personality().refusesToFly()) return;
        if (TardisMindSystem.stress(tardis) >= OBEDIENT_REVEAL_STRESS) personality(tardis);
    }

    /** If it is stressed or has enough trauma it reveals that it lands upset. */
    private static void onLand(Tardis tardis) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        boolean upset = mind.stress() >= NOTICEABLE || TardisMindSystem.trauma(tardis) >= NOTICEABLE;
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

    /** Only the owner gets these. */
    private static List<ServerPlayer> audience(Tardis tardis, String kind) {
        ServerPlayer owner = tardis.ownerPlayer().orElse(null);
        if (owner == null) return List.of();
        if (!"disability".equals(kind) && !isThere(tardis, owner)) return List.of();
        return List.of(owner);
    }

    /** Being inside always counts, outside the owner must be near the exterior. */
    private static boolean isThere(Tardis tardis, ServerPlayer owner) {
        if (owner.level() == tardis.interiorLevel().orElse(null)) return true;
        ServerLevel outside = tardis.exteriorLevel().orElse(null);
        BlockPos box = tardis.exteriorPos().orElse(null);
        return outside != null
                && box != null
                && owner.level() == outside
                && owner.distanceToSqr(Vec3.atCenterOf(box)) <= NEARBY_RANGE * NEARBY_RANGE;
    }

    /** Does nothing if the advancement does not exist. */
    private static void award(ServerPlayer player, String path) {
        MinecraftServer server = player.level().getServer();
        AdvancementHolder holder = server.getAdvancements().get(GCUtil.modIdentifier(path));
        if (holder != null) player.getAdvancements().award(holder, "discovered");
    }
}
