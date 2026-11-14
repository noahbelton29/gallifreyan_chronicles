package uk.nbforge.gallifreyan_chronicles.tardis.mind.action;

import net.minecraft.core.BlockPos;
import java.util.function.BiConsumer;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem.Refusal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/* what's going on when a hook runs. level, player and pos are only there when the hook has them */
public final class MindContext {
    public final Tardis tardis;
    public final @Nullable ServerLevel level;
    public final @Nullable Player player;
    public final @Nullable Vec3 pos;

    /* who pulled the lever, when it's "when_unwilling_to_fly". null means the owner */
    public @Nullable UUID pilot;
    /* how far through something it is, 0 to 1. used by terror */
    public float progress;
    /* how afraid of the player it is, 0 to 1. used by "when_you_enter" */
    public float severity;
    /* set by actions on "on_unwilling" to decide what the lever does */
    public @Nullable Refusal refusal;
    /* set when an action replaces the normal reaction */
    public boolean handled;

    public MindContext(Tardis tardis, @Nullable ServerLevel level, @Nullable Player player, @Nullable Vec3 pos) {
        this.tardis = tardis;
        this.level = level;
        this.player = player;
        this.pos = pos;
    }

    /* the player this is about: the one holding the mallet, or the pilot */
    public @Nullable UUID who() {
        return player != null ? player.getUUID() : pilot;
    }

    /* runs at the console inside and the box outside */
    public void everywhere(BiConsumer<ServerLevel, Vec3> run) {
        ServerLevel inside = tardis.interiorLevel().orElse(null);
        BlockPos console = tardis.consolePos().orElse(null);
        if (inside != null && console != null) run.accept(inside, Vec3.atCenterOf(console));

        ServerLevel outside = tardis.exteriorLevel().orElse(null);
        BlockPos box = tardis.exteriorPos().orElse(null);
        if (outside != null && box != null) run.accept(outside, Vec3.atCenterOf(box));
    }

    /* runs where this happened, or everywhere if it didn't happen anywhere (like a refusal) */
    public void here(BiConsumer<ServerLevel, Vec3> run) {
        if (level != null && pos != null) run.accept(level, pos);
        else everywhere(run);
    }

    public static MindContext of(Tardis tardis) {
        return new MindContext(tardis, null, null, null);
    }
}
