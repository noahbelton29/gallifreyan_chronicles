package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.GCDimensions;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;

public final class TardisDestinations {
    public static final int[] INCREMENTS = {1, 10, 100, 1000, 10000};

    private static final int WORLD_LIMIT = 29_999_999;

    private static final float[] FACING_YAWS = {180f, 225f, 270f, 315f, 0f, 45f, 90f, 135f};
    private static final String[] FACING_KEYS = {
        "facing.gallifreyan_chronicles.north",
        "facing.gallifreyan_chronicles.north_east",
        "facing.gallifreyan_chronicles.east",
        "facing.gallifreyan_chronicles.south_east",
        "facing.gallifreyan_chronicles.south",
        "facing.gallifreyan_chronicles.south_west",
        "facing.gallifreyan_chronicles.west",
        "facing.gallifreyan_chronicles.north_west"
    };

    public enum Axis {
        X,
        Y,
        Z
    }

    private TardisDestinations() {}

    public static int increment(Tardis tardis) {
        TardisData data = tardis.data();
        int amount = INCREMENTS[Mth.clamp(data.getCoordIncrementIndex(), 0, INCREMENTS.length - 1)];
        return data.isCoordIncrementNegative() ? -amount : amount;
    }

    public static @Nullable LandingTarget current(Tardis tardis) {
        LandingTarget target = tardis.landingTarget().orElse(null);
        if (target != null) return target;

        BlockPos pos = tardis.exteriorPos().orElse(null);
        if (pos == null) return null;

        float yaw = tardis.exterior().map(TardisExteriorBlockEntity::getYaw).orElse(0f);
        return new LandingTarget(tardis.exteriorDimension(), pos, yaw);
    }

    public static int toggleNegative(Tardis tardis) {
        TardisData data = tardis.data();
        data.setCoordIncrementNegative(!data.isCoordIncrementNegative());
        return increment(tardis);
    }

    public static int cycleIncrement(Tardis tardis) {
        TardisData data = tardis.data();
        data.setCoordIncrementIndex((data.getCoordIncrementIndex() + 1) % INCREMENTS.length);
        return increment(tardis);
    }

    public static int cycleIncrementBack(Tardis tardis) {
        TardisData data = tardis.data();
        int count = INCREMENTS.length;
        data.setCoordIncrementIndex((data.getCoordIncrementIndex() - 1 + count) % count);
        return increment(tardis);
    }

    public static @Nullable LandingTarget step(Tardis tardis, Axis axis) {
        LandingTarget target = current(tardis);
        if (target == null) return null;

        int amount = increment(tardis);
        BlockPos pos = target.pos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        switch (axis) {
            case X -> x = wrapWorld(x + (long) amount);
            case Z -> z = wrapWorld(z + (long) amount);
            case Y -> {
                ServerLevel level = tardis.server().getLevel(target.dimension());
                y = level == null ? y + amount : wrapHeight(level, y + (long) amount);
            }
        }

        LandingTarget updated = new LandingTarget(target.dimension(), new BlockPos(x, y, z), target.yaw());
        tardis.data().setLandingTarget(updated);
        return updated;
    }

    public static @Nullable LandingTarget rotate(Tardis tardis, int direction) {
        LandingTarget target = current(tardis);
        if (target == null) return null;

        int next = Math.floorMod(facingIndex(target.yaw()) + direction, FACING_YAWS.length);
        LandingTarget updated = new LandingTarget(target.dimension(), target.pos(), FACING_YAWS[next]);
        tardis.data().setLandingTarget(updated);
        return updated;
    }

    public static Component facingName(float yaw) {
        return Component.translatable(FACING_KEYS[facingIndex(yaw)]);
    }

    private static int facingIndex(float yaw) {
        float wrapped = Mth.positiveModulo(yaw, 360f);
        int best = 0;
        float bestDiff = Float.MAX_VALUE;
        for (int i = 0; i < FACING_YAWS.length; i++) {
            float diff = Math.abs(Mth.wrapDegrees(wrapped - FACING_YAWS[i]));
            if (diff < bestDiff) {
                bestDiff = diff;
                best = i;
            }
        }
        return best;
    }

    public static @Nullable LandingTarget nextDimension(Tardis tardis) {
        return stepDimension(tardis, 1);
    }

    public static @Nullable LandingTarget previousDimension(Tardis tardis) {
        return stepDimension(tardis, -1);
    }

    private static @Nullable LandingTarget stepDimension(Tardis tardis, int direction) {
        LandingTarget target = current(tardis);
        if (target == null) return null;

        List<ServerLevel> levels = landableLevels(tardis.server());
        if (levels.isEmpty()) return null;

        int index = -1;
        for (int i = 0; i < levels.size(); i++) {
            if (levels.get(i).dimension().equals(target.dimension())) {
                index = i;
                break;
            }
        }
        int nextIndex =
                index < 0 ? (direction > 0 ? 0 : levels.size() - 1) : Math.floorMod(index + direction, levels.size());
        ServerLevel next = levels.get(nextIndex);

        BlockPos pos = target.pos();
        int y = Mth.clamp(pos.getY(), next.getMinY(), next.getMaxY() - 1);
        LandingTarget updated =
                new LandingTarget(next.dimension(), new BlockPos(pos.getX(), y, pos.getZ()), target.yaw());
        tardis.data().setLandingTarget(updated);
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
            if (isLandable(level)) levels.add(level);
        }
        levels.sort(Comparator.comparing(
                (ServerLevel level) -> level.dimension().identifier().toString()));
        return levels;
    }

    private static boolean isLandable(ServerLevel level) {
        if (Tardis.idOfLevel(level).isPresent()) return false;
        if (level.dimension().equals(GCDimensions.TARDIS_INTERIOR_LEVEL_KEY)) return false;
        Identifier id = level.dimension().identifier();
        return !id.getNamespace().equals(Constants.MOD_ID) || !id.getPath().startsWith("tardis");
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
        player.sendOverlayMessage(Component.translatable(
                        "overlay.gallifreyan_chronicles.destination",
                        Component.literal(axis.name() + " " + value).withStyle(ChatFormatting.WHITE),
                        dimensionName(target.dimension()))
                .withStyle(ChatFormatting.GOLD));
    }

    public static void showDimension(@Nullable Player player, MinecraftServer server, LandingTarget target) {
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable(
                        "overlay.gallifreyan_chronicles.destination_dimension",
                        dimensionName(target.dimension()),
                        Component.literal(landableIndex(server, target.dimension()) + "/" + landableCount(server))
                                .withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.GOLD));
    }

    public static void showDirection(@Nullable Player player, LandingTarget target) {
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable(
                        "overlay.gallifreyan_chronicles.destination_direction",
                        facingName(target.yaw()).copy().withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.GOLD));
    }

    public static void showIncrement(@Nullable Player player, int increment) {
        if (player == null) return;
        player.sendOverlayMessage(Component.translatable(
                        "overlay.gallifreyan_chronicles.coord_increment",
                        Component.literal(String.valueOf(increment)).withStyle(ChatFormatting.WHITE))
                .withStyle(ChatFormatting.GOLD));
    }

    public static Component dimensionName(ResourceKey<Level> dimension) {
        StringBuilder name = new StringBuilder();
        for (String word : dimension.identifier().getPath().split("[_/]")) {
            if (word.isEmpty()) continue;
            if (!name.isEmpty()) name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return Component.literal(name.toString()).withStyle(ChatFormatting.AQUA);
    }
}
