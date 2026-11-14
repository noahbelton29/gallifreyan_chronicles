package uk.nbforge.gallifreyan_chronicles.util;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PlayerMotionUtil {
    private PlayerMotionUtil() {
    }

    public static void set(Entity entity, Vec3 motion) {
        entity.setDeltaMovement(motion);
        sendToClient(entity);
    }

    public static void push(Entity entity, double x, double y, double z) {
        entity.push(x, y, z);
        sendToClient(entity);
    }

    private static void sendToClient(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }
}
