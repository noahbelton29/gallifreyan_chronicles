package uk.nbforge.gallifreyan_chronicles.tardis.mind.data;

import uk.nbforge.gallifreyan_chronicles.Constants;

/** An id with or without the mod namespace means the same thing. */
public final class MindIds {
    private MindIds() {}

    public static String normalize(String id) {
        return id.contains(":") ? id : Constants.MOD_ID + ":" + id;
    }
}
