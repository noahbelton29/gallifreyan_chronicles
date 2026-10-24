package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

public abstract class FadeType {
    private final String id;

    protected FadeType(String id) {
        this.id = id;
    }

    public final String id() {
        return id;
    }

    public abstract float opacity(float gone, float elapsed);

    @Override
    public String toString() {
        return id;
    }
}
