package uk.nbforge.gallifreyan_chronicles.tardis.mind;


/* how a tardis feels about one player.
   hits: unforgiven mallet hits, kindness takes them away again.
   trauma: scars from this player, they fade slowly on their own */
public record TardisBond(float trust, float fear, long fearTick, int hits, boolean betrayed,
                         float trauma, long traumaTick) {
    public TardisBond {
        trust = Math.clamp(trust, 0f, TardisMind.MAX);
        fear = Math.clamp(fear, 0f, TardisMind.MAX);
        hits = Math.max(0, hits);
        trauma = Math.clamp(trauma, 0f, TardisMind.MAX);
    }

    public static TardisBond fresh(float trust) {
        return new TardisBond(trust, 0f, 0L, 0, false, 0f, 0L);
    }

    /* has this player hurt it and not been forgiven yet */
    public boolean hurtIt() {
        return hits > 0;
    }

    public float traumaAt(long now) {
        return TardisMind.faded(trauma, traumaTick, now);
    }

    public TardisBond withTrust(float value) {
        return new TardisBond(value, fear, fearTick, hits, betrayed, trauma, traumaTick);
    }

    public TardisBond withFear(float value, long now) {
        return new TardisBond(trust, value, now, hits, betrayed, trauma, traumaTick);
    }

    public TardisBond withHits(int value) {
        return new TardisBond(trust, fear, fearTick, value, betrayed, trauma, traumaTick);
    }

    public TardisBond withBetrayed(boolean value) {
        return new TardisBond(trust, fear, fearTick, hits, value, trauma, traumaTick);
    }

    public TardisBond withTrauma(float value, long now) {
        return new TardisBond(trust, fear, fearTick, hits, betrayed, value, now);
    }
}
