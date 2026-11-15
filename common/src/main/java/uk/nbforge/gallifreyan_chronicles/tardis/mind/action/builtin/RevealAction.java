package uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindContext;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

import java.util.Optional;

/**
   The TARDIS just showed who it is so the owner discovers its personality.
   if_it_reacted means only if an action before this really did something, like hit_back.
   traits also reveals traits with this revealed_when.
*/
public record RevealAction(boolean personality, boolean ifItReacted, Optional<Reveal> traits) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":reveal";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        if (ifItReacted && !context.handled) return;
        if (personality) TardisDiscovery.personality(context.tardis);
        traits.ifPresent(trigger -> TardisDiscovery.reveal(context.tardis, trigger));
    }
}
