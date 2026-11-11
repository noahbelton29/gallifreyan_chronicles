package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;

/* {"type": "add_stress", "amount": 2} negative takes it away */
public record AddStressAction(float amount) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":add_stress";
    public static final MapCodec<AddStressAction> CODEC = Codec.FLOAT.fieldOf("amount")
            .xmap(AddStressAction::new, AddStressAction::amount);

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        TardisMindSystem.addStress(context.tardis, amount);
    }
}
