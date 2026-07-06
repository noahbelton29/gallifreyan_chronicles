package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GCBlockEntities {
    public static BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE;
    public static BlockEntityType<TardisConsoleBlockEntity> TARDIS_CONSOLE_BLOCK_ENTITY_TYPE;

    public static void registerBlockEntities() {
        Constants.LOG.info("Registered GC Block Entities");

        TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                GCUtils.of("tardis_block_entity"),
                FabricBlockEntityTypeBuilder.create(
                        TardisExteriorBlockEntity::new,
                        GCBlocks.TARDIS
                ).build()
        );

        TARDIS_CONSOLE_BLOCK_ENTITY_TYPE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                GCUtils.of("tardis_console_block_entity"),
                FabricBlockEntityTypeBuilder.create(
                        TardisConsoleBlockEntity::new,
                        GCBlocks.TARDIS_CONSOLE
                ).build()
        );

        TardisExteriorBlockEntity.TYPE = () -> TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE;
        TardisConsoleBlockEntity.TYPE = () -> TARDIS_CONSOLE_BLOCK_ENTITY_TYPE;
    }
}