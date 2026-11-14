package uk.nbforge.gallifreyan_chronicles.tardis.mind.data;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/* a min/max pair from json, picked from at random */
public record MindRange(float min, float max) {
    public static MindRange of(float value) {
        return new MindRange(value, value);
    }

    public float pick(RandomSource random) {
        return max <= min ? min : Mth.randomBetween(random, min, max);
    }

    public int pickInt(RandomSource random) {
        int low = Math.round(min);
        int high = Math.max(low, Math.round(max));
        return low + random.nextInt(high - low + 1);
    }
}
