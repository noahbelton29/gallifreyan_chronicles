package uk.nbforge.gallifreyan_chronicles.tardis.mind.mood;

import java.util.List;
import java.util.UUID;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisEvents;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;
import uk.nbforge.gallifreyan_chronicles.tardis.subsystem.TardisSubsystem;
import uk.nbforge.gallifreyan_chronicles.tardis.subsystem.TardisSubsystems;

public final class TardisMoods {
    private static final long OVERUSE_TICKS = 600L;

    private static final float OVERUSE_STRESS = 10f;
    private static final float DAMAGED_FLIGHT_STRESS = 8f;
    private static final float WRONG_KEY_STRESS = 4f;
    private static final float ABORTED_TAKEOFF_STRESS = 6f;

    private static final float CALM_LANDING_STRESS = 30f;

    private static final float CALM_LANDING_TRUST = 2f;

    private TardisMoods() {}

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisMoods::onTakeOff);
        TardisEvents.LAND.register(TardisMoods::onLand);
    }

    public static void onTakeoffForcedStop(Tardis tardis) {
        TardisMinds.addStress(tardis, ABORTED_TAKEOFF_STRESS);
    }

    public static void onWrongKey(Tardis tardis) {
        TardisMinds.addStress(tardis, WRONG_KEY_STRESS);
    }

    private static void onTakeOff(Tardis tardis) {
        if (tookOffTooSoon(tardis)) {
            TardisMinds.addStress(tardis, OVERUSE_STRESS);
        }
        if (hasDamagedSystems(tardis)) {
            TardisMinds.addStress(tardis, DAMAGED_FLIGHT_STRESS);
        }
    }

    private static void onLand(Tardis tardis) {
        if (TardisMinds.stress(tardis) < CALM_LANDING_STRESS) {
            for (UUID player : crew(tardis)) {
                if (TardisMinds.calmAround(tardis, player)) {
                    TardisMinds.addTrust(tardis, player, CALM_LANDING_TRUST);
                }
            }
        }
        TardisMinds.markLanded(tardis);
    }

    /** everyone aboard, or the owner if nobody's in the interior */
    public static List<UUID> crew(Tardis tardis) {
        List<UUID> aboard = tardis.interiorLevel()
                .map(level ->
                        level.players().stream().map(player -> player.getUUID()).toList())
                .orElse(List.of());
        return aboard.isEmpty() ? List.of(tardis.ownerId()) : aboard;
    }

    private static boolean tookOffTooSoon(Tardis tardis) {
        long lastLand = TardisMinds.mind(tardis).lastLand();
        long now = TardisMinds.now(tardis.server());
        return lastLand != TardisMind.NEVER && now - lastLand < OVERUSE_TICKS;
    }

    /** an unloaded exterior counts as having no faults */
    private static boolean hasDamagedSystems(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exteriorIfLoaded().orElse(null);
        if (exterior == null) return false;
        for (TardisSubsystem subsystem : TardisSubsystems.all()) {
            if (subsystem.isFaulty(exterior.getSubsystemHealth(subsystem))) return true;
        }
        return false;
    }
}
