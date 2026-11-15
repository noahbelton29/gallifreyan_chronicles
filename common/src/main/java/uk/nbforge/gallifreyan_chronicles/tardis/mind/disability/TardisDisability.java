package uk.nbforge.gallifreyan_chronicles.tardis.mind.disability;

import java.util.Arrays;
import java.util.Optional;

/** The disabilities a TARDIS can have, each with an id for saving and a chance of a new TARDIS having it. */
public enum TardisDisability {
    DYSLEXIC("dyslexic", 0.15f),

    /** PTSD is not something a TARDIS is born with, it only develops through abuse. */
    PTSD("ptsd", 0f);

    private final String id;
    private final float chance;

    TardisDisability(String id, float chance) {
        this.id = id;
        this.chance = chance;
    }

    public String id() {
        return id;
    }

    public float chance() {
        return chance;
    }

    public static Optional<TardisDisability> byId(String id) {
        return Arrays.stream(values()).filter(d -> d.id.equals(id)).findFirst();
    }
}