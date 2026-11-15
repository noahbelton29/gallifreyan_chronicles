package uk.nbforge.gallifreyan_chronicles.client.sound;

public interface OffsetChannel {
    void gc$setOffsetSeconds(float seconds);

    // Muffles the sound, 1 is clear and lower is more muffled by cutting the high sounds
    // Call it on the sound thread, ChannelHandle execute
    void gc$setMuffle(float highGain);
}
