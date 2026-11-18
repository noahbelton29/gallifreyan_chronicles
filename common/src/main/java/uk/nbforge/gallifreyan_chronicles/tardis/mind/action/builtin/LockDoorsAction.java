package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public record LockDoorsAction(boolean onlyOutside) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":lock_doors";
    private static final int CHECK_TICKS = 5;
    private static final int GIVE_UP_AFTER = 12;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        boolean outside = context.level != null
                && context.level != context.tardis.interiorLevel().orElse(null);
        if (!onlyOutside || outside) closeAndLock(context.tardis);
    }

    /** Doors can only lock once they are fully shut so shut them and keep checking. */
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
        SchedulerUtil.runLater(tardis.server(), CHECK_TICKS, () -> lockWhenShut(tardis, triesLeft - 1));
    }
}
