package uk.nbforge.gallifreyan_chronicles.tardis.mind.data;

import uk.nbforge.gallifreyan_chronicles.Constants;

/* "skittish" and "gallifreyan_chronicles:skittish" mean the same thing */
public final class MindIds {
    private MindIds() {
    }

    public static String normalize(String id) {
        return id.contains(":") ? id : Constants.MOD_ID + ":" + id;
    }
}
