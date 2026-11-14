package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.client.GCClientHooks;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record TardisConsoleAnimationPayload(Identifier dimension, BlockPos pos, String animationName, String controllerName) implements CustomPacketPayload {
    public static final Type<TardisConsoleAnimationPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_console_animation"));

    public TardisConsoleAnimationPayload(Identifier dimension, BlockPos pos, String animationName) {
        this(dimension, pos, animationName, "controls");
    }

    public void apply() {
        GCClientHooks.applyConsoleAnimation(dimension, pos, animationName, controllerName);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}