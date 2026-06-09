package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;

public class GCModelProvider extends FabricModelProvider {
    private static final int GALLIFREYAN_GRASS_TINT = 0xFFE0E0E0;

    public GCModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        registerCubeBlocks(generators);
        registerPlants(generators);
        registerGrassBlock(generators);
        registerCadonwoodSet(generators);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

    }

    private void registerCubeBlocks(BlockModelGenerators generators) {
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_DIRT);
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_SAND);
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_STONE);
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_COBBLESTONE);
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_GRAVEL);
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_MUD);
        generators.createTrivialCube(GCBlocks.ARCADIAN_SHALE);
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_STONE_IRON_ORE);
        generators.createTrivialCube(GCBlocks.GALLIFREYAN_STONE_COAL_ORE);
    }

    private void registerPlants(BlockModelGenerators generators) {
        generators.createCrossBlockWithDefaultItem(
                GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS,
                BlockModelGenerators.PlantType.NOT_TINTED
        );

        generators.createCrossBlockWithDefaultItem(
                GCBlocks.TALL_GALLIFREYAN_DRY_GRASS,
                BlockModelGenerators.PlantType.NOT_TINTED
        );

        generators.createDoublePlant(
                GCBlocks.TALL_GALLIFREYAN_GRASS,
                BlockModelGenerators.PlantType.TINTED
        );

        generators.registerSimpleTintedItemModel(
                GCBlocks.TALL_GALLIFREYAN_GRASS,
                ModelLocationUtils.getModelLocation(
                        GCBlocks.TALL_GALLIFREYAN_GRASS,
                        "_top"
                ),
                ItemModelUtils.constantTint(GALLIFREYAN_GRASS_TINT)
        );

        generators.createCrossBlock(
                GCBlocks.SHORT_GALLIFREYAN_GRASS,
                BlockModelGenerators.PlantType.TINTED
        );

        Identifier itemModel =
                generators.createFlatItemModelWithBlockTexture(
                        GCBlocks.SHORT_GALLIFREYAN_GRASS.asItem(),
                        GCBlocks.SHORT_GALLIFREYAN_GRASS
                );

        generators.registerSimpleTintedItemModel(
                GCBlocks.SHORT_GALLIFREYAN_GRASS,
                itemModel,
                ItemModelUtils.constantTint(GALLIFREYAN_GRASS_TINT)
        );
    }

    private void registerGrassBlock(BlockModelGenerators generators) {
        TextureMapping normalMapping = new TextureMapping()
                .put(TextureSlot.BOTTOM,
                        TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_DIRT))
                .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
                .put(TextureSlot.TOP,
                        TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_GRASS_BLOCK, "_top"))
                .put(TextureSlot.SIDE,
                        TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_GRASS_BLOCK, "_side"));

        Identifier normalModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                normalMapping,
                generators.modelOutput
        );

        TextureMapping snowyMapping = new TextureMapping()
                .put(TextureSlot.BOTTOM,
                        TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_DIRT))
                .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
                .put(TextureSlot.TOP,
                        TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_GRASS_BLOCK, "_top"))
                .put(TextureSlot.SIDE,
                        TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_GRASS_BLOCK, "_snow"));

        MultiVariant snowyVariant = BlockModelGenerators.plainVariant(
                ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(
                        GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                        "_snow",
                        snowyMapping,
                        generators.modelOutput
                )
        );

        generators.createGrassLikeBlock(
                GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                BlockModelGenerators.createRotatedVariants(
                        new Variant(normalModel)
                ),
                snowyVariant
        );
    }

    private void registerCadonwoodSet(BlockModelGenerators generators) {
        generators.createTrivialCube(GCBlocks.CADONWOOD_PLANKS);

        generators.woodProvider(GCBlocks.CADONWOOD_LOG)
                .log(GCBlocks.CADONWOOD_LOG)
                .wood(GCBlocks.CADONWOOD_WOOD);

        generators.woodProvider(GCBlocks.STRIPPED_CADONWOOD_LOG)
                .log(GCBlocks.STRIPPED_CADONWOOD_LOG)
                .wood(GCBlocks.STRIPPED_CADONWOOD_WOOD);

        generators.createTrivialBlock(
                GCBlocks.CADONWOOD_LEAVES,
                TexturedModel.LEAVES
        );

        generators.createPlantWithDefaultItem(
                GCBlocks.CADONWOOD_SAPLING,
                GCBlocks.POTTED_CADONWOOD_SAPLING,
                BlockModelGenerators.PlantType.TINTED
        );
    }
}