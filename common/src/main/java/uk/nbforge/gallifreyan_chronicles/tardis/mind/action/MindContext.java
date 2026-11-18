package uk.nbforge.gallifreyan_chronicles.tardis.mind.action;

import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds.Refusal;

/** level, player and pos are only there when the hook has them */
public final class MindContext {
    public final Tardis tardis;
    public final @Nullable ServerLevel level;
    public final @Nullable Player player;
    public final @Nullable Vec3 pos;

    public @Nullable UUID pilot;
    public float progress;
    public float severity;
    public @Nullable Refusal refusal;
    public boolean handled;

    public MindContext(Tardis tardis, @Nullable ServerLevel level, @Nullable Player player, @Nullable Vec3 pos) {
        this.tardis = tardis;
        this.level = level;
        this.player = player;
        this.pos = pos;
    }

    /** the player this is about, whoevers holding the mallet or the pilot */
    public @Nullable UUID who() {
        return player != null ? player.getUUID() : pilot;
    }

    public void everywhere(BiConsumer<ServerLevel, Vec3> run) {
        ServerLevel inside = tardis.interiorLevel().orElse(null);
        BlockPos console = tardis.consolePos().orElse(null);
        if (inside != null && console != null) run.accept(inside, Vec3.atCenterOf(console));

        ServerLevel outside = tardis.exteriorLevel().orElse(null);
        BlockPos box = tardis.exteriorPos().orElse(null);
        if (outside != null && box != null) run.accept(outside, Vec3.atCenterOf(box));
    }

    /** runs where this happened, or everywhere if it didnt happen anywhere, like a refusal */
    public void here(BiConsumer<ServerLevel, Vec3> run) {
        if (level != null && pos != null) run.accept(level, pos);
        else everywhere(run);
    }

    public static MindContext of(Tardis tardis) {
        return new MindContext(tardis, null, null, null);
    }
}
