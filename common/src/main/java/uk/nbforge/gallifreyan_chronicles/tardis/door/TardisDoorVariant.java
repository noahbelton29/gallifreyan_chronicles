package uk.nbforge.gallifreyan_chronicles.tardis.door;

import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;

public record TardisDoorVariant(String id, String parent, String texture, boolean isDefault, boolean interior,
                                String exterior,
                                String openSound, String closeSound, boolean openAllOnly, int priority,
                                String block, String particle, float cooldownSeconds)
        implements IdentifiableData<TardisDoorVariant> {

    public static final int NO_PRIORITY = Integer.MAX_VALUE;
    /* how long the primary door waits between toggles; set "cooldown_seconds" in the variant json to change it */
    public static final float DEFAULT_COOLDOWN_SECONDS = 3.0f;

    public String localId() {
        return GCUtil.localPath(id);
    }

    @Override
    public TardisDoorVariant withId(String id) {
        return new TardisDoorVariant(id, parent, texture, isDefault, interior, exterior, openSound, closeSound, openAllOnly, priority, block, particle, cooldownSeconds);
    }
}
