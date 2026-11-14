package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;

/* {"type": "lock_doors", "only_outside": true} shuts the doors then locks them.
   only_outside: only if the player is outside it */
public record LockDoorsAction(boolean onlyOutside) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":lock_doors";
    public static final MapCodec<LockDoorsAction> CODEC = Codec.BOOL.optionalFieldOf("only_outside", false)
            .xmap(LockDoorsAction::new, LockDoorsAction::onlyOutside);

    private static final int CHECK_TICKS = 5;
    private static final int GIVE_UP_AFTER = 12;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        boolean outside = context.level != null && context.level != context.tardis.interiorLevel().orElse(null);
        if (!onlyOutside || outside) closeAndLock(context.tardis);
    }

    /* doors can only lock once they're fully shut, so shut them and keep checking */
    public static void closeAndLock(Tardis tardis) {
        if (tardis.isLocked()) return;
        tardis.closeDoors();
        lockWhenShut(tardis, GIVE_UP_AFTER);
    }

    private static void lockWhenShut(Tardis tardis, int triesLeft) {
        if (tardis.areDoorsFullyClosed()) {
            tardis.lock();
            return;
        }
        if (triesLeft <= 0) return;
        GCScheduler.runLater(tardis.server(), CHECK_TICKS, () -> lockWhenShut(tardis, triesLeft - 1));
    }
}
