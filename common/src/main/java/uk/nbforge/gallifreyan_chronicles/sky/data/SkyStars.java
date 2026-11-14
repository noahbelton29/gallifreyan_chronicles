package uk.nbforge.gallifreyan_chronicles.sky.data;


public record SkyStars(int count, long seed, float minSize, float maxSize, float brightness, int color,
                       boolean fadesInRain, float twinkle, float twinkleSpeed) {
}
