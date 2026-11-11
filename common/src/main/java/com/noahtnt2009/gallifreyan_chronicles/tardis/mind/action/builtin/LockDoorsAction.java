package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;

/* {"type": "lock_doors", "only_outside": true} only_outside: only if the player is outside it */
public record LockDoorsAction(boolean onlyOutside) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":lock_doors";
    public static final MapCodec<LockDoorsAction> CODEC = Codec.BOOL.optionalFieldOf("only_outside", false)
            .xmap(LockDoorsAction::new, LockDoorsAction::onlyOutside);

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        boolean outside = context.level != null && context.level != context.tardis.interiorLevel().orElse(null);
        if (!onlyOutside || outside) context.tardis.lock();
    }
}
