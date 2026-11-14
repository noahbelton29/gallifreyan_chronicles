package uk.nbforge.gallifreyan_chronicles.tardis.mind;

/* fear of a player as one ladder. each step adds one thing, they don't all pile on at once:
   uneasy: lights dip and the hum warps when they come aboard
   afraid: controls sometimes stick for them, and it flinches at the mallet
   terrified: it freezes up and won't move for them at all */
public enum FearLevel {
    CALM, UNEASY, AFRAID, TERRIFIED;

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
