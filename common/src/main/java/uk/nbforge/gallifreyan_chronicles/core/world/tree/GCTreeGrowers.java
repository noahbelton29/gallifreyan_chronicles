package uk.nbforge.gallifreyan_chronicles.core.world.tree;

import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.GCConfiguredFeatures;

public class GCTreeGrowers {
    public static final TreeGrower CADONWOOD = new TreeGrower(
            Constants.MOD_ID + ":cadonwood",
            WeightedList.of(GCConfiguredFeatures.CADONWOOD_TREE),
            WeightedList.of(),
            WeightedList.of(),
            GCConfiguredFeatures.CADONWOOD_TREE);
}
