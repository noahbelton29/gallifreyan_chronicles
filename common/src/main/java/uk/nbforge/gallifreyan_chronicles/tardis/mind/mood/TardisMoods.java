package uk.nbforge.gallifreyan_chronicles.tardis.mind.mood;

import java.util.UUID;
import java.util.List;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.SubsystemSystem;

/**
 * Handles changes to a TARDIS's mood in response to flight events.
 * <p>
 * Taking off too soon, flying with damaged systems, and using the wrong key
 * can increase stress. A calm landing can improve trust with the crew.
 */
public final class TardisMoods {
    /**
     * Minimum time between landings and take-offs before overuse causes stress.
     */
    private static final long OVERUSE_TICKS = 600L;

    private static final float OVERUSE_STRESS = 10f;
    private static final float DAMAGED_FLIGHT_STRESS = 8f;
    private static final float WRONG_KEY_STRESS = 4f;

    /**
     * Maximum stress level at which a landing is considered calm.
     */
    private static final float CALM_LANDING_STRESS = 30f;

    private static final float CALM_LANDING_TRUST = 2f;

    private TardisMoods() {
    }

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisMoods::onTakeOff);
        TardisEvents.LAND.register(TardisMoods::onLand);
    }

    public static void onWrongKey(Tardis tardis) {
        TardisMindSystem.addStress(tardis, WRONG_KEY_STRESS);
    }

    private static void onTakeOff(Tardis tardis) {
        if (tookOffTooSoon(tardis)) {
            TardisMindSystem.addStress(tardis, OVERUSE_STRESS);
        }
        if (hasDamagedSystems(tardis)) {
            TardisMindSystem.addStress(tardis, DAMAGED_FLIGHT_STRESS);
        }
    }

    private static void onLand(Tardis tardis) {
        if (TardisMindSystem.stress(tardis) < CALM_LANDING_STRESS) {
            for (UUID player : crew(tardis)) {
                if (!TardisMindSystem.afraidOf(tardis, player)) {
                    TardisMindSystem.addTrust(tardis, player, CALM_LANDING_TRUST);
                }
            }
        }
        TardisMindSystem.markLanded(tardis);
    }

    /**
     * Gets everyone currently aboard the TARDIS.
     * <p>
     * If nobody is in the interior, the TARDIS owner is used instead.
     *
     * @param tardis the TARDIS whose crew is being checked
     * @return UUIDs of the players considered to be aboard
     */
    public static List<UUID> crew(Tardis tardis) {
        List<UUID> aboard = tardis.interiorLevel()
                .map(level -> level.players().stream().map(player -> player.getUUID()).toList())
                .orElse(List.of());
        return aboard.isEmpty() ? List.of(tardis.ownerId()) : aboard;
    }

    /**
     * Checks whether the TARDIS is taking off too soon after its previous landing.
     *
     * @param tardis the TARDIS being checked
     * @return {@code true} if it has been less than {@link #OVERUSE_TICKS}
     * since its last landing
     */
    private static boolean tookOffTooSoon(Tardis tardis) {
        long lastLand = TardisMindSystem.mind(tardis).lastLand();
        long now = TardisMindSystem.now(tardis.server());
        return lastLand != TardisMind.NEVER && now - lastLand < OVERUSE_TICKS;
    }

    /**
     * Checks whether any of the TARDIS's subsystems are faulty.
     * <p>
     * An unloaded exterior is treated as having no detected faults.
     *
     * @param tardis the TARDIS being checked
     * @return {@code true} if at least one subsystem is faulty
     */
    private static boolean hasDamagedSystems(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exteriorIfLoaded().orElse(null);
        if (exterior == null) return false;
        for (TardisSubsystem subsystem : TardisSubsystem.values()) {
            if (SubsystemSystem.isFaulty(exterior.asEntity(), subsystem)) return true;
        }
        return false;
    }
}