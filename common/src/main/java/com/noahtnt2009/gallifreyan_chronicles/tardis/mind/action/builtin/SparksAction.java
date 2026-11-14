package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import com.noahtnt2009.gallifreyan_chronicles.util.GCSparks;

/* {"type": "sparks", "size": "big", "height": 1}
   size: small (a crackle), big (a burst) or blast (an explosion) */
public record SparksAction(Size size, double height) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":sparks";
    public static final MapCodec<SparksAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            MindEnums.codec(Size.class).optionalFieldOf("size", Size.SMALL).forGetter(SparksAction::size),
            Codec.DOUBLE.optionalFieldOf("height", 1.0).forGetter(SparksAction::height)
    ).apply(inst, SparksAction::new));

    public enum Size {
        SMALL, BIG, BLAST
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        context.here((level, pos) -> {
            var at = pos.add(0, height, 0);
            switch (size) {
                case SMALL -> GCSparks.small(level, at, 10);
                case BIG -> GCSparks.big(level, at);
                case BLAST -> GCSparks.blast(level, at);
            }
        });
    }
}
