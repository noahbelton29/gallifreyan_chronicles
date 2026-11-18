package uk.nbforge.gallifreyan_chronicles.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;

public final class MessageUtil {
    private static final String PREFIX = "message." + Constants.MOD_ID + ".";
    private static final double NEARBY_RANGE = 8.0;

    private MessageUtil() {}

    public static MutableComponent text(String key, Object... args) {
        return Component.translatable(PREFIX + key, args);
    }

    public static void overlay(@Nullable Player player, String key, Object... args) {
        if (player != null) player.sendOverlayMessage(text(key, args));
    }

    public static void overlay(@Nullable Player player, ChatFormatting colour, String key, Object... args) {
        if (player != null) player.sendOverlayMessage(text(key, args).withStyle(colour));
    }

    public static void overlayAll(ServerLevel level, Component message) {
        for (ServerPlayer player : level.players()) {
            player.sendOverlayMessage(message);
        }
    }

    public static void overlayNearby(ServerLevel level, Vec3 pos, ChatFormatting colour, String key, Object... args) {
        Component message = text(key, args).withStyle(colour);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos) <= NEARBY_RANGE * NEARBY_RANGE) {
                player.sendOverlayMessage(message);
            }
        }
    }
}
