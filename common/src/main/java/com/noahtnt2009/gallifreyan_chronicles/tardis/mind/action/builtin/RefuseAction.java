package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem.Refusal;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;

/* {"type": "refuse"} the lever snaps back and it won't fly */
public record RefuseAction() implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":refuse";
    public static final MapCodec<RefuseAction> CODEC = MapCodec.unit(new RefuseAction());

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.refusal = Refusal.REFUSE;
    }
}
