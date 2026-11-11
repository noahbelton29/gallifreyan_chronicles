package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;

/* "skittish" and "gallifreyan_chronicles:skittish" mean the same thing */
public final class MindIds {
    public static final Codec<String> CODEC = Codec.STRING.xmap(MindIds::normalize, id -> id);

    private MindIds() {
    }

    public static String normalize(String id) {
        return id.contains(":") ? id : Constants.MOD_ID + ":" + id;
    }
}
