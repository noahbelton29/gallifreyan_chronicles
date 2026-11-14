package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal.TardisConvincing;

/* {"type": "balk"} snaps the lever back, but gives in if you keep pulling */
public record BalkAction() implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":balk";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.refusal = TardisConvincing.pull(context.tardis, context.pilot);
    }
}
