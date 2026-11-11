package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem.Refusal;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.refusal.TardisAnger;

/* {"type": "fight", "stress": 2} refuses and gets angrier each time, until it lashes out */
public record FightAction(float stress) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":fight";
    public static final MapCodec<FightAction> CODEC = Codec.FLOAT.optionalFieldOf("stress", 2f)
            .xmap(FightAction::new, FightAction::stress);

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
