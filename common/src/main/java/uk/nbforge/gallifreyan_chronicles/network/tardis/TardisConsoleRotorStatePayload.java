package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.client.GCClientHooks;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record TardisConsoleRotorStatePayload(Identifier dimension, BlockPos pos, String rotorState) implements CustomPacketPayload {
    public static final Type<TardisConsoleRotorStatePayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_console_rotor_state"));

    public void apply() {
        GCClientHooks.applyConsoleRotorState(dimension, pos, rotorState);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
