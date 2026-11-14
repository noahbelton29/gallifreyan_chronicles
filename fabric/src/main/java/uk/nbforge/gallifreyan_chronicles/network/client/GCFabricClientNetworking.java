package uk.nbforge.gallifreyan_chronicles.network.client;

import uk.nbforge.gallifreyan_chronicles.network.tardis.RematCatchPayload;
import uk.nbforge.gallifreyan_chronicles.network.ResumeSoundPayload;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleAnimationPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleRotorStatePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisAlarmPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisFrightPayload;
import uk.nbforge.gallifreyan_chronicles.network.DiscoveryToastPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisCrashRumblePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisExteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisInteriorSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class GCFabricClientNetworking {
    private GCFabricClientNetworking() {
    }

    public static void registerClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(TardisExteriorSyncPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisInteriorSyncPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisConsoleSyncPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(DimensionSkySyncPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisConsoleAnimationPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisConsoleRotorStatePayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisDrumShakePayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisAlarmPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisFrightPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(DiscoveryToastPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisCrashRumblePayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(RematCatchPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(ResumeSoundPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        Constants.LOG.info("Registered GC Client Networking (Fabric)");
    }
}