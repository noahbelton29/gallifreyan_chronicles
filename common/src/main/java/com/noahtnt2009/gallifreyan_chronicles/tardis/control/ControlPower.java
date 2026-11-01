package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;

import java.util.Set;

public final class ControlPower {
    private static final Set<String> USABLE_WITHOUT_POWER = Set.of(
            GCTardisControls.POWER_LEVER,
            GCTardisControls.DOOR_CONTROL,
            GCTardisControls.DOOR_LOCK_CONTROL
    );

    private ControlPower() {
    }

    public static boolean canUse(TardisConsoleBlockEntity console, String controlId) {
        return console.hasPower() || USABLE_WITHOUT_POWER.contains(controlId);
    }
}
