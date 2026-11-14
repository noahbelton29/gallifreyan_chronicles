package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static void registerBlockEntities(IEventBus eventBus) {
        TardisExteriorBlockEntity.TYPE = BLOCK_ENTITIES.register("tardis_block_entity", () ->
                new BlockEntityType<>(TardisExteriorBlockEntity::new, GCBlocks.TARDIS));
        TardisConsoleBlockEntity.TYPE = BLOCK_ENTITIES.register("tardis_console_entity", () ->
                new BlockEntityType<>(TardisConsoleBlockEntity::new, GCBlocks.TARDIS_CONSOLE));
        InteriorDoorBlockEntity.TYPE = BLOCK_ENTITIES.register("interior_door_block_entity", () ->
                new BlockEntityType<>(InteriorDoorBlockEntity::new, GCBlocks.interiorDoorBlocks()));
        BLOCK_ENTITIES.register(eventBus);

        Constants.LOG.info("Registered GC Block Entities (NeoForge)");
    }
}