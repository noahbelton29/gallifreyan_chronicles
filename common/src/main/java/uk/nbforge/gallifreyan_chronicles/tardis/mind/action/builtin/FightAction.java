package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem.Refusal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.refusal.TardisAnger;

/* {"type": "fight", "stress": 2} refuses and gets angrier each time, until it lashes out */
public record FightAction(float stress) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":fight";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        TardisAnger.provoke(context.tardis, stress, context.pilot);
        context.refusal = Refusal.REFUSE;
    }
}
