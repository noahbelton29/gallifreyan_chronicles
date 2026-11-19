package uk.nbforge.gallifreyan_chronicles.network.tardis;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlPower;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public record CoordinateIncrementScrollPayload(int entityId, int direction) implements CustomPacketPayload {
    public static final Type<CoordinateIncrementScrollPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("coordinate_increment_scroll"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoordinateIncrementScrollPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    CoordinateIncrementScrollPayload::entityId,
                    ByteBufCodecs.VAR_INT,
                    CoordinateIncrementScrollPayload::direction,
                    CoordinateIncrementScrollPayload::new);

    public void handle(ServerPlayer player) {
        if (!(player.level().getEntity(entityId) instanceof TardisControlEntity entity)) return;
        if (!GCTardisControls.COORDINATE_STEP_CONTROL.equals(entity.getControlId())) return;
        if (!player.isWithinEntityInteractionRange(entity, 1.0)) return;

        TardisConsoleBlockEntity console = entity.getConsole();
        if (console == null) return;
        if (Tardis.cannotUseControls(console, player)) return;
        if (ControlPower.isBlocked(console, entity.getControlId())) return;

        Tardis tardis = Tardis.of(console).orElse(null);
        if (tardis == null) return;

        int increment = TardisDestinations.scaleIncrement(tardis, Integer.signum(direction));
        TardisDestinations.showIncrement(player, increment);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
