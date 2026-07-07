package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCArmorMaterials;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class GCModelProvider extends ModelProvider {
    private static final int GALLIFREYAN_GRASS_TINT = 0xFFE0E0E0;

    public GCModelProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(GCBlocks.MOONSLATE);
        blockModels.createTrivialCube(GCBlocks.MOON_DUST);
        blockModels.createTrivialCube(GCBlocks.LUNAR_REGOLITH);

        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_DIRT);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_SAND);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_STONE);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_COBBLESTONE);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_GRAVEL);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_MUD);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_MUDSTONE);
        blockModels.createTrivialCube(GCBlocks.ARCADIAN_SHALE);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_STONE_IRON_ORE);
        blockModels.createTrivialCube(GCBlocks.GALLIFREYAN_STONE_COAL_ORE);
        blockModels.createTrivialCube(GCBlocks.COBBLED_LIMESTONE);
        blockModels.createTrivialCube(GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE);
        blockModels.createNonTemplateModelBlock(GCBlocks.THORIVINE);
        blockModels.createFlatItemModel(GCBlocks.THORIVINE.asItem());
        blockModels.createCrossBlockWithDefaultItem(GCBlocks.THORIVINE_FLOWER,
                BlockModelGenerators.PlantType.NOT_TINTED);

        blockModels.createCrossBlockWithDefaultItem(
                GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS,
                BlockModelGenerators.PlantType.NOT_TINTED
        );

        blockModels.createCrossBlockWithDefaultItem(
                GCBlocks.TALL_GALLIFREYAN_DRY_GRASS,
                BlockModelGenerators.PlantType.NOT_TINTED
        );

        blockModels.createDoublePlant(
                GCBlocks.TALL_GALLIFREYAN_GRASS,
                BlockModelGenerators.PlantType.TINTED
        );

        blockModels.registerSimpleTintedItemModel(
                GCBlocks.TALL_GALLIFREYAN_GRASS,
                ModelLocationUtils.getModelLocation(
                        GCBlocks.TALL_GALLIFREYAN_GRASS,
                        "_top"
                ),
                ItemModelUtils.constantTint(GALLIFREYAN_GRASS_TINT)
        );

        blockModels.createCrossBlock(
                GCBlocks.SHORT_GALLIFREYAN_GRASS,
                BlockModelGenerators.PlantType.TINTED
        );

        Identifier itemModel =
                blockModels.createFlatItemModelWithBlockTexture(
                        GCBlocks.SHORT_GALLIFREYAN_GRASS.asItem(),
                        GCBlocks.SHORT_GALLIFREYAN_GRASS
                );

        blockModels.registerSimpleTintedItemModel(
                GCBlocks.SHORT_GALLIFREYAN_GRASS,
                itemModel,
                ItemModelUtils.constantTint(GALLIFREYAN_GRASS_TINT)
        );

        blockModels.createParticleOnlyBlock(GCBlocks.TARDIS);
        blockModels.createParticleOnlyBlock(GCBlocks.TARDIS_CONSOLE);
        itemModels.generateFlatItem(GCBlocks.TARDIS.asItem(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCBlocks.TARDIS_CONSOLE.asItem(), ModelTemplates.FLAT_ITEM);

        registerGrassBlock(blockModels);
        registerCadonwoodSet(blockModels);
        createCustomVine(blockModels, GCBlocks.GALLIFREYAN_VINE, GCBlocks.GALLIFREYAN_VINE.asItem());

        itemModels.generateFlatItem(GCItems.TARANIUM_SCRAP, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_CRYSTAL, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.REFINED_TARANIUM_CRYSTAL, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_INGOT, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateTrimmableItem(GCItems.TARANIUM_HELMET, GCArmorMaterials.TARANIUM_KEY, ItemModelGenerators.TRIM_PREFIX_HELMET, false);
        itemModels.generateTrimmableItem(GCItems.TARANIUM_CHESTPLATE, GCArmorMaterials.TARANIUM_KEY, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, false);
        itemModels.generateTrimmableItem(GCItems.TARANIUM_LEGGINGS, GCArmorMaterials.TARANIUM_KEY, ItemModelGenerators.TRIM_PREFIX_LEGGINGS, false);
        itemModels.generateTrimmableItem(GCItems.TARANIUM_BOOTS, GCArmorMaterials.TARANIUM_KEY, ItemModelGenerators.TRIM_PREFIX_BOOTS, false);
    }

    public void createCustomVine(BlockModelGenerators blockModelGenerators, Block vineBlock, Item vineItem) {
        blockModelGenerators.createMultifaceBlockStates(vineBlock);
        blockModelGenerators.createFlatItemModelWithBlockTexture(vineItem, vineBlock);
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
        generators.family(GCBlocks.CADONWOOD_PLANKS)
                .stairs(GCBlocks.CADONWOOD_STAIRS)
                .slab(GCBlocks.CADONWOOD_SLAB)
                .pressurePlate(GCBlocks.CADONWOOD_PRESSURE_PLATE)
                .button(GCBlocks.CADONWOOD_BUTTON)
                .fence(GCBlocks.CADONWOOD_FENCE)
                .fenceGate(GCBlocks.CADONWOOD_FENCE_GATE)
                .wall(GCBlocks.CADONWOOD_WALL);

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
