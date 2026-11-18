package uk.nbforge.gallifreyan_chronicles.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.CadonwoodBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.FloraBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.GallifreyBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.InteriorBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.MoonBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.RoundelBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.core.item.DoorBlockItem;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public final class GCBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();
    private static final List<Block> PICKABLE_DOORS = new ArrayList<>();

    private GCBlocks() {}

    public static void registerBlocks() {
        TardisBlocks.init();
        InteriorBlocks.init();
        GallifreyBlocks.init();
        MoonBlocks.init();
        FloraBlocks.init();
        CadonwoodBlocks.init();
        RoundelBlocks.init();
        GCLog.CORE.info("Registered GC Blocks");
    }

    public static Block register(String name, Function<BlockBehaviour.Properties, Block> function) {
        return register(name, function, Rarity.COMMON, BlockItem::new);
    }

    public static Block register(
            String name,
            Function<BlockBehaviour.Properties, Block> function,
            Rarity rarity,
            BiFunction<Block, Item.Properties, Item> itemFactory) {
        Block block = registerWithoutItem(name, function);
        Item item = Registry.register(
                BuiltInRegistries.ITEM,
                GCUtil.modIdentifier(name),
                itemFactory.apply(
                        block,
                        new Item.Properties()
                                .useBlockDescriptionPrefix()
                                .rarity(rarity)
                                .setId(ResourceKey.create(Registries.ITEM, GCUtil.modIdentifier(name)))));
        if (item instanceof DoorBlockItem) PICKABLE_DOORS.add(block);
        return block;
    }

    public static Block registerWithoutItem(String name, Function<BlockBehaviour.Properties, Block> function) {
        Block block = function.apply(
                BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, GCUtil.modIdentifier(name))));
        BLOCKS.add(block);
        return Registry.register(BuiltInRegistries.BLOCK, GCUtil.modIdentifier(name), block);
    }

    public static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    public static boolean neverViewBlocking(BlockState state, BlockGetter level, BlockPos pos, AABB box) {
        return false;
    }

    public static Block[] interiorDoorBlocks() {
        return BLOCKS.stream()
                .filter(block -> block instanceof InteriorDoorBlock)
                .toArray(Block[]::new);
    }

    public static List<Block> pickableDoors() {
        return List.copyOf(PICKABLE_DOORS);
    }
}
