package com.noahtnt2009.gallifreyan_chronicles.world.tree;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.world.GCConfiguredFeatures;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class GCTreeGrowers {
    public static final TreeGrower CADONWOOD = new TreeGrower(Constants.MOD_ID + ":cadonwood",
            Optional.empty(), Optional.of(GCConfiguredFeatures.CADONWOOD_TREE), Optional.empty());
}
