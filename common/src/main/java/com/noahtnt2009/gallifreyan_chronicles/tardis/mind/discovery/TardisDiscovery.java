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

/**
 * Handles when players discover things about a TARDIS's personality.
 * <p>
 * A TARDIS doesn't reveal everything about itself straight away.
 * Players learn about its personality, traits and disabilities through
 * certain events and interactions.
 * <p>
 * Discoveries are only recorded when the owner is around to witness them.
 */
public final class TardisDiscovery {

    /**
     * The main advancement that is unlocked when the TARDIS mind is discovered.
     */
    private static final String ROOT = "tardis_mind/root";

    /**
     * How much stress or trauma a TARDIS needs before showing that it's upset.
     */
    private static final float NOTICEABLE = 5f;

    /**
     * How stressed an obedient TARDIS must be before its personality is revealed
     * through taking off despite normally refusing to fly.
     */
    private static final float OBEDIENT_REVEAL_STRESS = 50f;

    /**
     * How close the owner needs to be to the exterior to witness a discovery.
     */
    private static final double NEARBY_RANGE = 24.0;

    private TardisDiscovery() {
    }

    /**
     * Registers the events that can reveal parts of a TARDIS's personality.
     */
    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisDiscovery::onTakeOff);
        TardisEvents.LAND.register(TardisDiscovery::onLand);
    }

    /**
     * Checks whether taking off reveals the TARDIS's personality.
     * <p>
     * A TARDIS that normally refuses to fly won't reveal its personality
     * just by taking off. However, if it's obedient and under enough stress,
     * taking off can reveal something about how it behaves.
     *
     * @param tardis the TARDIS that is taking off
     */
    private static void onTakeOff(Tardis tardis) {
        if (TardisMindSystem.mind(tardis).personality().refusesToFly()) return;
        if (TardisMindSystem.stress(tardis) >= OBEDIENT_REVEAL_STRESS) personality(tardis);
    }

    /**
     * Checks how the TARDIS feels when it lands.
     * <p>
     * If it's stressed or has built up enough trauma, it reveals that
     * it's landing while upset.
     *
     * @param tardis the TARDIS that has landed
     */
    private static void onLand(Tardis tardis) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        boolean upset = mind.stress() >= NOTICEABLE || TardisMindSystem.trauma(tardis) >= NOTICEABLE;
        if (!upset) return;
        reveal(tardis, Reveal.IT_LANDS_UPSET);
    }

    /**
     * Reveals which personality the TARDIS has.
     *
     * @param tardis the TARDIS whose personality is being revealed
     */
    public static void personality(Tardis tardis) {
        String id = TardisMindSystem.mind(tardis).personality().id();
        reveal(tardis, "personality", id, "personality", "tardis_mind/personality");
    }

    /**
     * Checks the TARDIS's traits for any that should be revealed by this event.
     *
     * @param tardis  the TARDIS being checked
     * @param trigger the event that can reveal a trait
     */
    public static void reveal(Tardis tardis, Reveal trigger) {
        for (TardisTrait trait : TardisMindSystem.mind(tardis).traits()) {
            if (trait.reveal() == trigger) trait(tardis, trait);
        }
    }

    /**
     * Reveals a trait if the TARDIS has it and the owner hasn't discovered it yet.
     *
     * @param tardis the TARDIS with the trait
     * @param trait  the trait being revealed
     */
    public static void trait(Tardis tardis, TardisTrait trait) {
        TardisMind mind = TardisMindSystem.mind(tardis);
        if (!mind.has(trait)) return;
        if (mind.knows("trait:" + trait.localId())) return;
        reveal(tardis, "trait:" + trait.id(), trait.id(), "trait", "tardis_mind/trait");
    }

    /**
     * Reveals a disability if the TARDIS has it.
     *
     * @param tardis     the TARDIS being checked
     * @param disability the disability being revealed
     */
    public static void disability(Tardis tardis, TardisDisability disability) {
        if (!TardisMindSystem.mind(tardis).has(disability)) return;
        reveal(tardis, "disability:" + disability.id(), disability.id(), "disability", "tardis_mind/" + disability.id());
    }

    /**
     * Records a discovery and tells the owner about it.
     * <p>
     * The owner must be around to witness the discovery. For most discoveries,
     * this means being inside the TARDIS or standing close to its exterior.
     * Disabilities are handled separately and don't require the owner to be nearby.
     * <p>
     * Once discovered, the same thing won't be revealed again.
     *
     * @param tardis      the TARDIS the discovery belongs to
     * @param key         the key used to remember this discovery
     * @param id          the ID sent to the discovery toast
     * @param kind        what type of thing was discovered
     * @param advancement the advancement to award
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

    /**
     * Finds the player who is allowed to see the discovery.
     * <p>
     * Only the TARDIS owner can receive these discoveries.
     *
     * @param tardis the TARDIS making the discovery
     * @param kind   what is being discovered
     * @return the owner if they're allowed to see it, otherwise an empty list
     */
    private static List<ServerPlayer> audience(Tardis tardis, String kind) {
        ServerPlayer owner = tardis.ownerPlayer().orElse(null);
        if (owner == null) return List.of();
        if (!"disability".equals(kind) && !isThere(tardis, owner)) return List.of();
        return List.of(owner);
    }

    /**
     * Checks whether the owner is close enough to the TARDIS to witness something.
     * <p>
     * Being inside the TARDIS always counts. Outside, the owner needs to be
     * within the allowed distance of its exterior.
     *
     * @param tardis the TARDIS being checked
     * @param owner  the TARDIS owner
     * @return true if the owner is inside or close enough to the exterior
     */
    private static boolean isThere(Tardis tardis, ServerPlayer owner) {
        if (owner.level() == tardis.interiorLevel().orElse(null)) return true;
        ServerLevel outside = tardis.exteriorLevel().orElse(null);
        BlockPos box = tardis.exteriorPos().orElse(null);
        return outside != null && box != null && owner.level() == outside
                && owner.distanceToSqr(Vec3.atCenterOf(box)) <= NEARBY_RANGE * NEARBY_RANGE;
    }

    /**
     * Gives the player the advancement for discovering something about the TARDIS.
     * <p>
     * If the advancement doesn't exist, nothing happens.
     *
     * @param player the player receiving the advancement
     * @param path   the advancement's resource path
     */
    private static void award(ServerPlayer player, String path) {
        MinecraftServer server = player.level().getServer();
        AdvancementHolder holder = server.getAdvancements().get(GCUtils.modIdentifier(path));
        if (holder != null) player.getAdvancements().award(holder, "discovered");
    }
}