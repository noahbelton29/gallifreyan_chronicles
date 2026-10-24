package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

public final class ClassicFadeType extends FadeType {
    public ClassicFadeType() {
        super("classicwho");
    }

    @Override
    public float opacity(float gone, float elapsed) {
        return 1.0f - gone;
    }
}
