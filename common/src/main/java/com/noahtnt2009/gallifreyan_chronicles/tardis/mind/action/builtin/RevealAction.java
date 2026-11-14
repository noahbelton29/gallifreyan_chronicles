package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;

import java.util.Optional;

/* {"type": "reveal", "if_it_reacted": true}
   the tardis just showed who it is, so the owner discovers its personality.
   if_it_reacted: only if an action before this actually did something (like hit_back)
   traits: also reveal traits with this "revealed_when" */
public record RevealAction(boolean personality, boolean ifItReacted, Optional<Reveal> traits) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":reveal";
    public static final MapCodec<RevealAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.optionalFieldOf("personality", true).forGetter(RevealAction::personality),
            Codec.BOOL.optionalFieldOf("if_it_reacted", false).forGetter(RevealAction::ifItReacted),
            MindEnums.codec(Reveal.class).optionalFieldOf("traits").forGetter(RevealAction::traits)
    ).apply(inst, RevealAction::new));

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
