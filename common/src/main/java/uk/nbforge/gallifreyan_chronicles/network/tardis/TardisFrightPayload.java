package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.client.tardis.TardisFrightClient;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.NonNull;

/* the tardis is afraid of you: warps the console hum and makes the lights stutter for a while */
public record TardisFrightPayload(float severity, int ticks, Identifier dimension) implements CustomPacketPayload {
    public static final Type<TardisFrightPayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_fright"));

    public void apply() {
        TardisFrightClient.start(severity, ticks, ResourceKey.create(Registries.DIMENSION, dimension));
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
