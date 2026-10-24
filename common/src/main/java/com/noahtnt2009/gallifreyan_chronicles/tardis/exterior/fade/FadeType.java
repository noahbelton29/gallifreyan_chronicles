package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;

public abstract class FadeType implements IdentifiableData<FadeType> {
    private final String id;
    private final FadeTiming timing;

    protected FadeType(String id, FadeTiming timing) {
        this.id = id;
        this.timing = timing;
    }

    @Override
    public final String id() {
        return id;
    }

    public final FadeTiming timing() {
        return timing;
    }

    public abstract MapCodec<? extends FadeType> codec();

    public abstract float opacity(float gone, float elapsed);

    @Override
    public abstract FadeType withId(String id);

    @Override
    public String toString() {
        return id;
    }
}
