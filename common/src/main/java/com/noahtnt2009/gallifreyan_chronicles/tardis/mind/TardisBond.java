
package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Stores how a TARDIS feels about an individual player.
 *
 * <p>Trust, fear and past actions are tracked separately for each player.
 */
public record TardisBond(float trust, float fear, long fearTick, int hits, boolean betrayed) {

    /**
     * Codec for loading and saving a player's bond with a TARDIS.
     */
    public static final Codec<TardisBond> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("trust", TardisMind.START_TRUST).forGetter(TardisBond::trust),
            Codec.FLOAT.optionalFieldOf("fear", 0f).forGetter(TardisBond::fear),
            Codec.LONG.optionalFieldOf("fear_tick", 0L).forGetter(TardisBond::fearTick),
            Codec.INT.optionalFieldOf("hits", 0).forGetter(TardisBond::hits),
            Codec.BOOL.optionalFieldOf("betrayed", false).forGetter(TardisBond::betrayed)
    ).apply(inst, TardisBond::new));

    public TardisBond {
        trust = Math.clamp(trust, 0f, TardisMind.MAX);
        fear = Math.clamp(fear, 0f, TardisMind.MAX);
    }

    /**
     * Creates a new bond with the given starting trust and no previous history.
     *
     * @param trust the player's starting trust
     */
    public static TardisBond fresh(float trust) {
        return new TardisBond(trust, 0f, 0L, 0, false);
    }

    /**
     * Checks whether this player has ever hurt the TARDIS.
     */
    public boolean hurtIt() {
        return hits > 0;
    }

    public TardisBond withTrust(float value) {
        return new TardisBond(value, fear, fearTick, hits, betrayed);
    }

    /**
     * Updates the player's fear and the tick when it was last changed.
     *
     * @param value the new fear value
     * @param now   the current game tick
     */
    public TardisBond withFear(float value, long now) {
        return new TardisBond(trust, value, now, hits, betrayed);
    }

    /**
     * Records another time this player has hurt the TARDIS.
     */
    public TardisBond hit() {
        return new TardisBond(trust, fear, fearTick, hits + 1, betrayed);
    }

    public TardisBond withBetrayed(boolean value) {
        return new TardisBond(trust, fear, fearTick, hits, value);
    }
}
