package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class ControlPower {
    private static final Set<String> USABLE_WITHOUT_POWER = Set.of(
            GCTardisControls.POWER_LEVER,
            GCTardisControls.DOOR_CONTROL,
            GCTardisControls.DOOR_LOCK_CONTROL
    );

    private static final Predicate<TardisConsoleBlockEntity> TRAVELLING =
            console -> TardisApi.of(console).map(Tardis::isTravelling).orElse(false);

    private static final Predicate<TardisConsoleBlockEntity> CLOAKED =
            console -> console.getControlState(GCTardisControls.CLOAK_CONTROL);

    private static final Map<String, List<Predicate<TardisConsoleBlockEntity>>> BLOCKED_WHEN = Map.of(
            GCTardisControls.POWER_LEVER, List.of(TRAVELLING, CLOAKED),
            GCTardisControls.FLIGHT_LEVER, List.of(CLOAKED)
    );

    private ControlPower() {
    }

    public static void validate() {
        for (String id : USABLE_WITHOUT_POWER) requireRegistered(id);
        for (String id : BLOCKED_WHEN.keySet()) requireRegistered(id);
    }

    private static void requireRegistered(String id) {
        if (!TardisControlRegistry.isRegistered(id)) {
            throw new IllegalStateException("ControlPower references unregistered TARDIS control: " + id);
        }
    }

    public static boolean canUse(TardisConsoleBlockEntity console, String controlId) {
        for (Predicate<TardisConsoleBlockEntity> blocker : BLOCKED_WHEN.getOrDefault(controlId, List.of())) {
            if (blocker.test(console)) return false;
        }
        return console.hasPower() || USABLE_WITHOUT_POWER.contains(controlId);
    }
}
