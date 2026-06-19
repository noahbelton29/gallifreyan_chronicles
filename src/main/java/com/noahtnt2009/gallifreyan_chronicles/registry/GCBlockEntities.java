package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class GCBlockEntities {
    public static final BlockEntityType<TardisExteriorBlockEntity> TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE =
         register("tardis_block_entity", FabricBlockEntityTypeBuilder.create(TardisExteriorBlockEntity::new, GCBlocks.TARDIS_BLOCK));

    public static <T extends BlockEntity> BlockEntityType<T> register(
            String id,
            FabricBlockEntityTypeBuilder<T> builder) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, id), builder.build());
    }

    public static void init() {}
}
