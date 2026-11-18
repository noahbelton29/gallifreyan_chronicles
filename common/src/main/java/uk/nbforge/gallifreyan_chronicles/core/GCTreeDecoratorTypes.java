package uk.nbforge.gallifreyan_chronicles.core;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.tree_decorators.CadonwoodTrunkVineDecorator;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class GCTreeDecoratorTypes {
    public static final TreeDecoratorType<CadonwoodTrunkVineDecorator> CADONWOOD_TRUNK_VINE = Registry.register(
            BuiltInRegistries.TREE_DECORATOR_TYPE,
            GCUtil.modIdentifier("cadonwood_trunk_vine"),
            new TreeDecoratorType<>(CadonwoodTrunkVineDecorator.CODEC));

    public static void registerTreeDecoratorTypes() {
        Constants.LOG.info("Registered GC Tree Decorator Types");
    }
}
