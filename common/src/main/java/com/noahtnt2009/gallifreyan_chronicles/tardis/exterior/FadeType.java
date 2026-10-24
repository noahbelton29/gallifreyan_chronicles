package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import java.util.Locale;

public enum FadeType {
    NEW_WHO("newwho"),
    CLASSIC_WHO("classicwho");

    private final String id;

    FadeType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static FadeType byId(String id) {
        if (id == null) return NEW_WHO;
        String lowered = id.toLowerCase(Locale.ROOT);
        for (FadeType type : values()) {
            if (type.id.equals(lowered)) return type;
        }
        return NEW_WHO;
    }

    public static boolean isValid(String id) {
        if (id == null) return false;
        String lowered = id.toLowerCase(Locale.ROOT);
        for (FadeType type : values()) {
            if (type.id.equals(lowered)) return true;
        }
        return false;
    }
}
