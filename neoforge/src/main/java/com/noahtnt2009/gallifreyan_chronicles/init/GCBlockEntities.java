package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
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
        BLOCK_ENTITIES.register(eventBus);
    }
}