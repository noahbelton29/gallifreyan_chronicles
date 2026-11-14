package uk.nbforge.gallifreyan_chronicles.tardis.mind.disability;


import java.util.Arrays;
import java.util.Optional;

/**
 * Lists the disabilities a TARDIS can have.
 * <p>
 * Each disability has an ID used when saving and loading it, along with
 * a chance of a newly created TARDIS having it.
 */
public enum TardisDisability {
    DYSLEXIC("dyslexic", 0.15f),

    // PTSD isn't something a TARDIS is born with. It can only develop through abuse.
    PTSD("ptsd", 0f);

    private final String id;
    private final float chance;

    TardisDisability(String id, float chance) {
        this.id = id;
        this.chance = chance;
    }

    /**
     * Gets the ID used to identify this disability when saving it.
     *
     * @return the disability's ID
     */
    public String id() {
        return id;
    }

    /**
     * Gets the chance of a newly created TARDIS having this disability.
     *
     * @return the chance, from 0 to 1
     */
    public float chance() {
        return chance;
    }

    /**
     * Finds a disability using its saved ID.
     *
     * @param id the ID to look for
     * @return the matching disability, or empty if there isn't one
     */
    public static Optional<TardisDisability> byId(String id) {
        return Arrays.stream(values()).filter(d -> d.id.equals(id)).findFirst();
    }
}