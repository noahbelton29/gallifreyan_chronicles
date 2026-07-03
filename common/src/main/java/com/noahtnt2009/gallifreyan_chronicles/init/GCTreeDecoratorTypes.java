package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.tree_decorators.CadonwoodTrunkVineDecorator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class GCTreeDecoratorTypes {
    public static final TreeDecoratorType<CadonwoodTrunkVineDecorator> CADONWOOD_TRUNK_VINE =
            Registry.register(
                    BuiltInRegistries.TREE_DECORATOR_TYPE,
                    Identifier.fromNamespaceAndPath(Constants.MOD_ID, "cadonwood_trunk_vine"),
                    new TreeDecoratorType<>(CadonwoodTrunkVineDecorator.CODEC)
            );

    public static void registerTreeDecoratorTypes() {
        Constants.LOG.info("Registered GC Tree Decorator Types");
    }
}