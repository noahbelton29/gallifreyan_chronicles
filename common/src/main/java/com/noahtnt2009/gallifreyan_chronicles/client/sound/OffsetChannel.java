package com.noahtnt2009.gallifreyan_chronicles.client.sound;

public interface OffsetChannel {
    void gc$setOffsetSeconds(float seconds);

    /* muffle the sound: 1 = clear, lower = more muffled (cuts the high sounds).
       call it on the sound thread (ChannelHandle.execute) */
    void gc$setMuffle(float highGain);
}
