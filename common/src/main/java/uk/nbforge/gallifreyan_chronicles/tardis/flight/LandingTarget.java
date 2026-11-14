package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record LandingTarget(ResourceKey<Level> dimension, BlockPos pos, float yaw) {
}
