package uk.nbforge.gallifreyan_chronicles.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class GCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static void registerBlockEntities(IEventBus eventBus) {
        TardisExteriorBlockEntity.TYPE = BLOCK_ENTITIES.register(
                "tardis_block_entity",
                () -> new BlockEntityType<>(TardisExteriorBlockEntity::new, TardisBlocks.TARDIS));
        TardisConsoleBlockEntity.TYPE = BLOCK_ENTITIES.register(
                "tardis_console_entity",
                () -> new BlockEntityType<>(TardisConsoleBlockEntity::new, TardisBlocks.TARDIS_CONSOLE));
        InteriorDoorBlockEntity.TYPE = BLOCK_ENTITIES.register(
                "interior_door_block_entity",
                () -> new BlockEntityType<>(InteriorDoorBlockEntity::new, GCBlocks.interiorDoorBlocks()));
        BLOCK_ENTITIES.register(eventBus);

        GCLog.CORE.info("Registered GC Block Entities (NeoForge)");
    }
}
