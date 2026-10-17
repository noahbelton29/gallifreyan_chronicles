package com.noahtnt2009.gallifreyan_chronicles.world.tree;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.world.GCConfiguredFeatures;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;

public class GCTreeGrowers {
    public static final TreeGrower CADONWOOD = new TreeGrower(Constants.MOD_ID + ":cadonwood",
            WeightedList.of(GCConfiguredFeatures.CADONWOOD_TREE),
            WeightedList.of(),
            WeightedList.of(),
            GCConfiguredFeatures.CADONWOOD_TREE);
}