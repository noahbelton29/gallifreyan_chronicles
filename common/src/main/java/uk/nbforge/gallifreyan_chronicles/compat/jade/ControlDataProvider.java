package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

import java.util.UUID;

/** The destination data lives on the server so the destination controls send what their tooltip needs from here. */
public class ControlDataProvider implements IServerDataProvider<EntityAccessor> {
    public static final ControlDataProvider INSTANCE = new ControlDataProvider();

    public static final String COORD = "TargetCoord";
    public static final String DIMENSION = "TargetDimension";
    public static final String DIMENSION_INDEX = "TargetDimensionIndex";
    public static final String YAW = "TargetYaw";
    public static final String STEP = "CoordStep";

    @Override
    public void appendServerData(@NonNull CompoundTag data, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof TardisControlEntity control)
                || !(accessor.getLevel() instanceof ServerLevel level)) return;
        TardisConsoleBlockEntity console = control.getConsole();
        if (console == null) return;
        UUID tardisId = TardisApi.of(console).map(Tardis::id).orElse(null);
        if (tardisId == null) return;

        MinecraftServer server = level.getServer();
        String id = control.getControlId();
        switch (id) {
            case GCTardisControls.X_INCREMENT_CONTROL, GCTardisControls.Y_INCREMENT_CONTROL,
                 GCTardisControls.Z_INCREMENT_CONTROL -> {
                LandingTarget target = TardisDestinations.current(server, tardisId);
                if (target == null) return;
                data.putInt(COORD, switch (id) {
                    case GCTardisControls.X_INCREMENT_CONTROL -> target.pos().getX();
                    case GCTardisControls.Y_INCREMENT_CONTROL -> target.pos().getY();
                    default -> target.pos().getZ();
                });
            }
            case GCTardisControls.COORDINATE_STEP_CONTROL -> data.putInt(STEP, TardisDestinations.increment(server, tardisId));
            case GCTardisControls.DIRECTIONAL_CONTROL -> {
                LandingTarget target = TardisDestinations.current(server, tardisId);
                if (target != null) data.putFloat(YAW, target.yaw());
            }
            case GCTardisControls.DIMENSIONAL_CONTROL -> {
                LandingTarget target = TardisDestinations.current(server, tardisId);
                if (target == null) return;
                data.putString(DIMENSION, TardisDestinations.dimensionName(target.dimension()).getString());
                data.putString(DIMENSION_INDEX, TardisDestinations.landableIndex(server, target.dimension())
                        + "/" + TardisDestinations.landableCount(server));
            }
            default -> {
            }
        }
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtil.modIdentifier("tardis_control_data");
    }
}
