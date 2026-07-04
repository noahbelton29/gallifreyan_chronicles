package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GCBlockEntities {
    public static BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE;

    public static void registerBlockEntities() {
        Constants.LOG.info("Registered GC Block Entities");

        TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "tardis_block_entity"),
                FabricBlockEntityTypeBuilder.create(
                        TardisExteriorBlockEntity::new,
                        GCBlocks.TARDIS
                ).build()
        );

        TardisExteriorBlockEntity.TYPE = () -> TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE;
    }
}