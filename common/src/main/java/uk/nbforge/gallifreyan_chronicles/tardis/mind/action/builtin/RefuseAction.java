package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem.Refusal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;

/* {"type": "refuse"} the lever snaps back and it won't fly */
public record RefuseAction() implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":refuse";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.refusal = Refusal.REFUSE;
    }
}
