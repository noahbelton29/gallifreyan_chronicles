package uk.nbforge.gallifreyan_chronicles.network;

import uk.nbforge.gallifreyan_chronicles.client.GCClientHooks;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ResumeSoundPayload(Identifier sound, double x, double y, double z, float volume, float pitch,
                                 float offsetSeconds) implements CustomPacketPayload {
    public static final Type<ResumeSoundPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("resume_sound"));

    public void apply() {
        GCClientHooks.playResumedSound(this);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
