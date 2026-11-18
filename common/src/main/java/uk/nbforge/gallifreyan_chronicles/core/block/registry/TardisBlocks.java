package uk.nbforge.gallifreyan_chronicles.core.block.registry;

import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.register;
import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.registerWithoutItem;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBarrierBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisTopBlock;
import uk.nbforge.gallifreyan_chronicles.core.item.ConsoleBlockItem;
import uk.nbforge.gallifreyan_chronicles.core.item.DoorBlockItem;
import uk.nbforge.gallifreyan_chronicles.core.item.InteriorDoorBlockItem;
import uk.nbforge.gallifreyan_chronicles.core.item.TardisBlockItem;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorBlockJson;

public final class TardisBlocks {
    private TardisBlocks() {}

    public static final Block TARDIS = register(
            "tardis",
            properties -> new TardisBlock(properties
                    .strength(-1f, 3600000.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(TardisBlock.LIGHT))),
            Rarity.EPIC,
            TardisBlockItem::new);

    public static final Block TARDIS_TOP = registerWithoutItem(
            "tardis_top",
            properties -> new TardisTopBlock(properties
                    .strength(-1f, 3600000.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noLootTable()));

    public static final Block INTERIOR_DOOR_BARRIER = registerWithoutItem(
            "interior_door_barrier",
            properties -> new InteriorDoorBarrierBlock(properties
                    .strength(-1f, -1f)
                    .noOcclusion()
                    .noLootTable()
                    .isSuffocating(GCBlocks::never)
                    .isViewBlocking(GCBlocks::neverViewBlocking)
                    .pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block TARDIS_CONSOLE = register(
            "tardis_console",
            properties -> new ConsoleBlock(
                    properties.strength(-1f, -1f).sound(SoundType.STONE).noOcclusion()),
            Rarity.EPIC,
            ConsoleBlockItem::new);

    public static final Block TARDIS_INTERIOR_DOOR = register(
            "tardis_interior_door",
            properties -> new InteriorDoorBlock(
                    "first_doctors_interior_doors",
                    properties
                            .strength(-1f, 360000.0f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.POPPED)),
            Rarity.COMMON,
            InteriorDoorBlockItem::new);

    static {
        for (DoorBlockJson.Entry door : DoorBlockJson.entries()) {
            register(
                    door.block(),
                    properties -> new InteriorDoorBlock(
                            door.model(),
                            door.variant(),
                            properties
                                    .strength(3f, 3f)
                                    .sound(SoundType.STONE)
                                    .noOcclusion()
                                    .noLootTable()
                                    .pushReaction(PushReaction.POPPED)),
                    Rarity.COMMON,
                    DoorBlockItem::new);
        }
    }

    public static void init() {}
}
