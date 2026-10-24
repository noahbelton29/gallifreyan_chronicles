package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;

public final class NoopControl implements TardisControl {
    public static final NoopControl INSTANCE = new NoopControl();
    public static final MapCodec<NoopControl> CODEC = MapCodec.unit(INSTANCE);

    private NoopControl() {
    }

    @Override
    public MapCodec<? extends TardisControl> codec() {
        return CODEC;
    }
}
