package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;

/* {"type": "add_stress", "amount": 2} negative takes it away */
public record AddStressAction(float amount) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":add_stress";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        TardisMindSystem.addStress(context.tardis, amount);
    }
}
