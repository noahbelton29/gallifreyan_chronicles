package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import net.minecraft.util.RandomSource;

import java.util.List;

public final class HaywireControls {
    private static final int MAX_FLICKS_PER_CONSOLE = 3;

    private HaywireControls() {
    }

    public static void flick(Tardis tardis, RandomSource random) {
        for (TardisConsoleBlockEntity console : tardis.consoles()) {
            List<TardisControlEntity> controls = console.getControlEntities().stream()
                    /* power and handbrake stay put */
                    .filter(control -> !control.getControlId().equals(GCTardisControls.POWER_LEVER))
                    .filter(control -> !control.getControlId().equals(GCTardisControls.HANDBRAKE))
                    .toList();
            if (controls.isEmpty()) continue;

            int flicks = 1 + random.nextInt(MAX_FLICKS_PER_CONSOLE);
            for (int i = 0; i < flicks; i++) {
                TardisControlEntity control = controls.get(random.nextInt(controls.size()));
                console.playControlAnimationOnly(control.getControlId(), random.nextBoolean());
            }
        }
    }

    public static void settle(Tardis tardis) {
        for (TardisConsoleBlockEntity console : tardis.consoles()) {
            for (TardisControlEntity control : console.getControlEntities()) {
                String id = control.getControlId();
                console.playControlAnimationOnly(id, console.getControlState(id));
            }
        }
    }
}
