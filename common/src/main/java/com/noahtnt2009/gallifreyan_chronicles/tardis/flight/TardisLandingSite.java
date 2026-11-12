package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Finds safe landing positions for a TARDIS.
 *
 * <p>Searches nearby columns for a suitable position, preferring locations
 * close to the requested position with enough room to land.
 */
public final class TardisLandingSite {
    /**
     * Maximum horizontal distance from the requested position to search.
     */
    public static final int MAX_RADIUS = 12;

    /**
     * Maximum vertical distance to check in each column.
     */
    private static final int VERTICAL_WINDOW = 24;

    /**
     * Weight applied to vertical distance when calculating the landing score.
     */
    private static final double VERTICAL_WEIGHT = 0.35;

    /**
     * Penalty applied to positions surrounded by solid blocks.
     */
    private static final double ENCLOSED_PENALTY = 100.0;

    private record Candidate(BlockPos feet, double score) {
    }

    private TardisLandingSite() {
    }

    /**
     * Finds the best available landing position near the requested position.
     *
     * <p>If no suitable position is found within the search radius, the
     * original column is checked for the nearest available position.
     *
     * @param level  server level to search in
     * @param target requested landing position
     * @param own    current TARDIS position, if it is in the same level
     * @return the best landing position, or {@code null} if none is available
     */
    public static @Nullable BlockPos find(ServerLevel level, BlockPos target, @Nullable BlockPos own) {
        Candidate best = null;

        for (int radius = 0; radius <= MAX_RADIUS; radius++) {
            // Every column outside this radius is at least this far away horizontally.
            // If it cannot beat the current score, there is no reason to keep searching.
            if (best != null && (double) radius * radius >= best.score()) break;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // Only check the outer edge of the current radius.
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;

                    double horizontal = (double) dx * dx + (double) dz * dz;

                    if (best != null && horizontal >= best.score()) continue;

                    BlockPos column = new BlockPos(target.getX() + dx, target.getY(), target.getZ() + dz);

                    if (!level.getWorldBorder().isWithinBounds(column)) continue;

                    // Ensure the chunk is loaded before checking its blocks.
                    level.getChunkAt(column);

                    Candidate found = bestInColumn(level, column, horizontal, own);

                    if (found != null && (best == null || found.score() < best.score())) {
                        best = found;
                    }
                }
            }
        }

        if (best != null) return best.feet();

        return nearestInColumn(level, target, own);
    }

    private static @Nullable Candidate bestInColumn(ServerLevel level, BlockPos column, double horizontal,
                                                    @Nullable BlockPos own) {
        int minY = level.getMinY() + 1;
        int maxY = highestFeet(level);
        Candidate best = null;

        for (int dy = 0; dy <= VERTICAL_WINDOW; dy++) {
            for (int side = 0; side < 2; side++) {
                // The target height is only checked once.
                if (dy == 0 && side == 1) continue;

                int y = side == 0 ? column.getY() - dy : column.getY() + dy;

                if (y < minY || y > maxY) continue;

                BlockPos feet = new BlockPos(column.getX(), y, column.getZ());

                if (!standable(level, feet, own)) continue;

                double score = horizontal + VERTICAL_WEIGHT * dy * dy + opennessPenalty(level, feet);

                if (best == null || score < best.score()) {
                    best = new Candidate(feet, score);
                }
            }

            // Once the next vertical distance cannot beat the current score,
            // checking higher positions in this column is unnecessary.
            double next = horizontal + VERTICAL_WEIGHT * (dy + 1.0) * (dy + 1.0);

            if (best != null && next >= best.score()) break;
        }

        return best;
    }

    private static @Nullable BlockPos nearestInColumn(ServerLevel level, BlockPos target, @Nullable BlockPos own) {
        int minY = level.getMinY() + 1;
        int maxY = highestFeet(level);
        int range = maxY - minY;

        for (int dy = 0; dy <= range; dy++) {
            BlockPos below = target.below(dy);

            if (below.getY() >= minY && below.getY() <= maxY && standable(level, below, own)) return below;

            BlockPos above = target.above(dy);

            if (above.getY() >= minY && above.getY() <= maxY && standable(level, above, own)) return above;
        }

        return null;
    }

    /**
     * Gets the highest valid Y position for a TARDIS landing.
     *
     * <p>Dimensions with a ceiling use their logical height to avoid
     * searching above the Nether roof.
     *
     * @param level server level being checked
     * @return highest valid Y position for the TARDIS
     */
    private static int highestFeet(ServerLevel level) {
        if (level.dimensionType().hasCeiling()) {
            return level.getMinY() + level.dimensionType().logicalHeight() - 3;
        }

        return level.getMaxY() - 2;
    }

    /**
     * Checks whether a position has enough room for a TARDIS to land.
     *
     * <p>Requires two clear blocks of space above a solid, non-harmful floor.
     *
     * @param level server level being checked
     * @param feet  position where the TARDIS would stand
     * @param own   current TARDIS position, if available
     * @return {@code true} if the position is suitable for landing
     */
    public static boolean standable(ServerLevel level, BlockPos feet, @Nullable BlockPos own) {
        BlockPos below = feet.below();
        BlockPos head = feet.above();

        if (level.isOutsideBuildHeight(below) || level.isOutsideBuildHeight(head)) return false;

        // Allow the TARDIS to overlap its own blocks, but not land inside them.
        if (own != null && !feet.equals(own) && (feet.equals(own.above()) || head.equals(own))) return false;

        if (isBlocked(level, feet, own) || isBlocked(level, head, own)) return false;

        BlockState floor = level.getBlockState(below);

        if (!floor.getFluidState().isEmpty()) return false;
        if (floor.is(Blocks.MAGMA_BLOCK)) return false;

        return floor.isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean isBlocked(ServerLevel level, BlockPos pos, @Nullable BlockPos own) {
        // Treat the TARDIS's existing blocks as clear so it can land at its current position.
        if (own != null && (pos.equals(own) || pos.equals(own.above()))) return false;

        BlockState state = level.getBlockState(pos);

        if (!state.getFluidState().isEmpty()) return true;

        return !state.isAir() && !isSoft(state);
    }

    private static double opennessPenalty(ServerLevel level, BlockPos feet) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = feet.relative(direction);
            BlockState state = level.getBlockState(side);

            if (state.getCollisionShape(level, side).isEmpty() || isSoft(state)) return 0.0;
        }

        return ENCLOSED_PENALTY;
    }

    /**
     * Checks whether a block can be safely removed when the TARDIS lands.
     *
     * <p>Blocks containing block entities are never considered soft, as they
     * may contain stored data that should not be destroyed.
     *
     * @param state block state to check
     * @return {@code true} if the block can be replaced during landing
     */
    public static boolean isSoft(BlockState state) {
        if (state.isAir() || state.hasBlockEntity()) return false;

        if (state.canBeReplaced()) return true;

        if (state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)) return true;

        return state.is(Blocks.VINE) || state.is(Blocks.GLOW_LICHEN) || state.is(Blocks.SNOW)
                || state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.HANGING_ROOTS)
                || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)
                || state.is(Blocks.WEEPING_VINES) || state.is(Blocks.WEEPING_VINES_PLANT)
                || state.is(Blocks.TWISTING_VINES) || state.is(Blocks.TWISTING_VINES_PLANT);
    }

    /**
     * Removes a soft block so the TARDIS can occupy its position.
     *
     * <p>Blocks are destroyed without dropping items. Blocks containing
     * fluids are left untouched.
     *
     * @param level server level containing the block
     * @param pos   position of the block to remove
     */
    public static void clearSoft(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (isSoft(state) && state.getFluidState().isEmpty()) {
            level.destroyBlock(pos, false);
        }
    }
}