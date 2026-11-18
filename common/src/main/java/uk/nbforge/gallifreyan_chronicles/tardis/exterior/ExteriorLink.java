package uk.nbforge.gallifreyan_chronicles.tardis.exterior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;

public final class ExteriorLink {
    private ExteriorLink() {}

    private static Optional<TardisExteriorBlockEntity> findExterior(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior) {
            return Optional.of(exterior);
        }
        return Optional.empty();
    }

    public static void link(Tardis tardis, ServerLevel level, BlockPos pos) {
        findExterior(level, pos).ifPresent(exterior -> exterior.linkToTardis(tardis.id()));
        TardisData data = tardis.data();
        data.setExteriorBlockPos(pos);
        data.setExteriorDimension(level.dimension());
    }

    public static void unlink(Tardis tardis) {
        tardis.exterior().ifPresent(TardisExteriorBlockEntity::unlinkFromTardis);
        tardis.data().setExteriorBlockPos(null);
    }
}
