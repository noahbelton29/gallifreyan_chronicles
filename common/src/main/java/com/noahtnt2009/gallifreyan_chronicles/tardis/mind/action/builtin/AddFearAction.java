package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;

/* {"type": "add_fear", "amount": 2} negative takes it away */
public record AddFearAction(float amount) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":add_fear";
    public static final MapCodec<AddFearAction> CODEC = Codec.FLOAT.fieldOf("amount")
            .xmap(AddFearAction::new, AddFearAction::amount);

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        TardisMindSystem.addFear(context.tardis, amount);
    }
}
