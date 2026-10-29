package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class TardisDestinations {
    public static final int[] INCREMENTS = {1, 10, 100, 1000, 10000};

    private static final int WORLD_LIMIT = 29_999_999;

    public enum Axis { X, Y, Z }

    private TardisDestinations() {
    }

    public static int increment(MinecraftServer server, UUID tardisId) {
        int index = TardisManager.get(server).getCoordIncrementIndex(tardisId);
        int amount = INCREMENTS[Mth.clamp(index, 0, INCREMENTS.length - 1)];
        return TardisManager.get(server).isCoordIncrementNegative(tardisId) ? -amount : amount;
    }

    public static @Nullable LandingTarget current(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        LandingTarget target = manager.getLandingTarget(tardisId);
        if (target != null) return target;

        BlockPos pos = manager.resolveExteriorBlockPos(tardisId);
        if (pos == null) return null;

        TardisExteriorBlockEntity exterior = DoorSyncSystem.findExterior(server, tardisId);
        float yaw = exterior == null ? 0f : exterior.getYaw();
        return new LandingTarget(manager.getExteriorDimension(tardisId), pos, yaw);
    }

    public static int toggleNegative(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        manager.setCoordIncrementNegative(tardisId, !manager.isCoordIncrementNegative(tardisId));
        return increment(server, tardisId);
    }

    public static int cycleIncrement(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        int next = (manager.getCoordIncrementIndex(tardisId) + 1) % INCREMENTS.length;
        manager.setCoordIncrementIndex(tardisId, next);
        return increment(server, tardisId);
    }

    public static @Nullable LandingTarget step(MinecraftServer server, UUID tardisId, Axis axis) {
        LandingTarget target = current(server, tardisId);
        if (target == null) return null;

        int amount = increment(server, tardisId);
        BlockPos pos = target.pos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        switch (axis) {
            case X -> x = wrapWorld(x + (long) amount);
            case Z -> z = wrapWorld(z + (long) amount);
            case Y -> {
                ServerLevel level = server.getLevel(target.dimension());
                y = level == null ? y + amount : wrapHeight(level, y + (long) amount);
            }
        }

        LandingTarget updated = new LandingTarget(target.dimension(), new BlockPos(x, y, z), target.yaw());
        TardisManager.get(server).setLandingTarget(tardisId, updated);
        return updated;
    }

    public static @Nullable LandingTarget nextDimension(MinecraftServer server, UUID tardisId) {
        return stepDimension(server, tardisId, 1);
    }

    public static @Nullable LandingTarget previousDimension(MinecraftServer server, UUID tardisId) {
        return stepDimension(server, tardisId, -1);
    }

    private static @Nullable LandingTarget stepDimension(MinecraftServer server, UUID tardisId, int direction) {
        LandingTarget target = current(server, tardisId);
        if (target == null) return null;

        List<ServerLevel> levels = landableLevels(server);
        if (levels.isEmpty()) return null;

        int index = -1;
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).dimension().equals(target.dimension())) {
                index = i;
                break;
            }
        }
        int nextIndex = index < 0 ? (direction > 0 ? 0 : levels.size() - 1)
                : Math.floorMod(index + direction, levels.size());
        ServerLevel next = levels.get(nextIndex);

        BlockPos pos = target.pos();
        int y = Mth.clamp(pos.getY(), next.getMinY(), next.getMaxY() - 1);
        LandingTarget updated = new LandingTarget(next.dimension(), new BlockPos(pos.getX(), y, pos.getZ()), target.yaw());
        TardisManager.get(server).setLandingTarget(tardisId, updated);
        return updated;
    }

    public static int landableIndex(MinecraftServer server, ResourceKey<Level> dimension) {
        List<ServerLevel> levels = landableLevels(server);
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).dimension().equals(dimension)) return i + 1;
        }
        return 0;
    }

    public static int landableCount(MinecraftServer server) {
        return landableLevels(server).size();
    }

    private static List<ServerLevel> landableLevels(MinecraftServer server) {
        List<ServerLevel> levels = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            if (!isTardisInterior(level)) levels.add(level);
        }
        levels.sort(Comparator.comparing((ServerLevel level) -> level.dimension().identifier().toString()));
        return levels;
    }

    private static boolean isTardisInterior(ServerLevel level) {
        if (DoorSyncSystem.tardisIdOf(level) != null) return true;
        if (level.dimension().equals(GCDimensions.TARDIS_INTERIOR_LEVEL_KEY)) return true;
        Identifier id = level.dimension().identifier();
        return id.getNamespace().equals(Constants.MOD_ID) && id.getPath().startsWith("tardis");
    }

    private static int wrapWorld(long value) {
        long span = 2L * (WORLD_LIMIT + 1L);
        return (int) (Math.floorMod(value + WORLD_LIMIT + 1L, span) - WORLD_LIMIT - 1L);
    }

    private static int wrapHeight(ServerLevel level, long value) {
        int min = level.getMinY();
        int max = level.getMaxY() - 1;
        long span = (long) max - min + 1L;
        return (int) (min + Math.floorMod(value - min, span));
    }

    public static void showDestination(@Nullable Player player, Axis axis, LandingTarget target) {
        if (player == null) return;
        BlockPos pos = target.pos();
        int value = switch (axis) {
            case X -> pos.getX();
            case Y -> pos.getY();
            case Z -> pos.getZ();
        };
        player.sendOverlayMessage(Component.translatable("overlay.gallifreyan_chronicles.destination",
                Component.literal(axis.name() + " " + value).withStyle(ChatFormatting.WHITE),
                dimensionName(target.dimension())).withStyle(ChatFormatting.GOLD));
    }

    public static void showDimension(@Nullable Player player, MinecraftServer server, LandingTarget target) {
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("overlay.gallifreyan_chronicles.destination_dimension",
                dimensionName(target.dimension()),
                Component.literal(landableIndex(server, target.dimension()) + "/" + landableCount(server))
                        .withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GOLD));
    }

    public static void showIncrement(@Nullable Player player, int increment) {
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable("overlay.gallifreyan_chronicles.coord_increment",
                Component.literal(String.valueOf(increment)).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.GOLD));
    }

    private static Component dimensionName(ResourceKey<Level> dimension) {
        StringBuilder name = new StringBuilder();
        for (String word : dimension.identifier().getPath().split("[_/]")) {
            if (word.isEmpty()) continue;
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return Component.literal(name.toString()).withStyle(ChatFormatting.AQUA);
    }
}
