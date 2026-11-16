package uk.nbforge.gallifreyan_chronicles.tardis.mind;

/**
 * Fear of a player as one ladder where each step adds one thing and they do not all pile on at once.
 * Uneasy means the lights dip and the hum warps when they come aboard.
 * Afraid means controls sometimes stick for them and it flinches at the mallet.
 * Terrified means it freezes up and will not move for them at all.
 */
public enum FearLevel {
    CALM,
    UNEASY,
    AFRAID,
    TERRIFIED;

    public static final float UNEASY_AT = 10f;
    public static final float AFRAID_AT = 30f;
    public static final float TERRIFIED_AT = 60f;

    public static FearLevel of(float fear) {
        if (fear >= TERRIFIED_AT) return TERRIFIED;
        if (fear >= AFRAID_AT) return AFRAID;
        if (fear >= UNEASY_AT) return UNEASY;
        return CALM;
    }

    public boolean atLeast(FearLevel other) {
        return ordinal() >= other.ordinal();
    }
}
