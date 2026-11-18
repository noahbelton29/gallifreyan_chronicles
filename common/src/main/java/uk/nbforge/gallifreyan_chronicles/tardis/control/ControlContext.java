package uk.nbforge.gallifreyan_chronicles.tardis.control;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;

/** server side only */
public record ControlContext(TardisConsoleBlockEntity console, ServerLevel level, Optional<Tardis> tardis) {

    public static @Nullable ControlContext of(@Nullable TardisConsoleBlockEntity console) {
        if (console == null || !(console.getLevel() instanceof ServerLevel level)) return null;
        return new ControlContext(console, level, Tardis.of(console));
    }

    public static @Nullable ControlContext of(TardisControlEntity entity) {
        return of(entity.getConsole());
    }

    public boolean state(String controlId) {
        return console.getControlState(controlId);
    }
}
