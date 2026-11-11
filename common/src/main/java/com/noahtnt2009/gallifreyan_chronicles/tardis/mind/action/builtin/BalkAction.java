package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal.TardisConvincing;

/* {"type": "balk"} snaps the lever back, but gives in if you keep pulling */
public record BalkAction() implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":balk";
    public static final MapCodec<BalkAction> CODEC = MapCodec.unit(new BalkAction());

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.refusal = TardisConvincing.pull(context.tardis, context.pilot);
    }
}
