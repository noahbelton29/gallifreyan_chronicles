package uk.nbforge.gallifreyan_chronicles.tardis.subsystem;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;

/** health goes 0 to MAX_HEALTH, add new ones in TardisSubsystems */
public abstract class TardisSubsystem {
    public static final float MAX_HEALTH = 100f;

    private final String id;
    private final List<String> oldIds;

    /** oldIds are ids this subsystem used to be saved under, they still load and work in commands. */
    protected TardisSubsystem(String id, String... oldIds) {
        this.id = id;
        this.oldIds = List.of(oldIds);
    }

    public final String id() {
        return id;
    }

    public final boolean matches(String someId) {
        return id.equals(someId) || oldIds.contains(someId);
    }

    public abstract boolean isFaulty(float health);

    /** server only, for a loaded exterior. damage effects like smoke go here */
    public void tick(ServerLevel level, TardisExteriorBlockEntity exterior, float health) {}

    @Override
    public String toString() {
        return id;
    }
}
