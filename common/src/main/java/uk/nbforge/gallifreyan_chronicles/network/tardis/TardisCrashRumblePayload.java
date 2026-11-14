package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.client.camera.GCCameraShake;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/* a harsh rumble for the whole crash. with stress set, it's the ongoing stressed flight rumble instead,
   strength 0 to 1 where 1 is as hard as a crash */
public record TardisCrashRumblePayload(double x, double y, double z, int ticks, boolean stress, float strength)
        implements CustomPacketPayload {
    public static final Type<TardisCrashRumblePayload> TYPE =
            new Type<>(GCUtil.modIdentifier("tardis_crash_rumble"));

    public static void broadcast(ServerLevel level, Vec3 origin, int ticks) {
        send(level, new TardisCrashRumblePayload(origin.x, origin.y, origin.z, ticks, false, 1f));
    }

    /* keeps the stress rumble going. send it about once a second while flying */
    public static void broadcastStress(ServerLevel level, Vec3 origin, float strength) {
        send(level, new TardisCrashRumblePayload(origin.x, origin.y, origin.z, 0, true, strength));
    }

    private static void send(ServerLevel level, TardisCrashRumblePayload payload) {
        for (ServerPlayer player : level.players()) {
            Services.NETWORK.sendTardisCrashRumble(player, payload);
        }
    }

    public void apply() {
        if (stress) GCCameraShake.stressRumble(new Vec3(x, y, z), strength);
        else GCCameraShake.crashRumble(new Vec3(x, y, z), ticks);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
