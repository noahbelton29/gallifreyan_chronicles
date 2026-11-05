package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.client.model.ConsoleVariantSelectProperty;
import com.noahtnt2009.gallifreyan_chronicles.client.model.TardisExteriorSelectProperty;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GCModelProvider extends ModelProvider {
    private static final int GALLIFREYAN_GRASS_TINT = 0xFFE0E0E0;

    // Cactus-shaped parent shipped in common resources (models/block/thorivine_base.json).
    // Kept in our own namespace so vanilla changes to the cactus model can't break Thorivine.
    private static final ModelTemplate THORIVINE_TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/thorivine_base")),
            Optional.empty(),
            TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.BOTTOM
    );

    private static final ModelTemplate TARDIS_FLYING = new ModelTemplate(
            Optional.of(
                    Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/tardis_base")
            ),
            Optional.of("_flying"),
            TextureSlot.PARTICLE
    ).extend().ambientOcclusion(false).build();

    public GCModelProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(GCBlocks.MOONSLATE);
        blockModels.createTrivialCube(GCBlocks.MOON_DUST);
        blockModels.createTrivialCube(GCBlocks.LUNAR_REGOLITH);

        TextureMapping tardisTextures = new TextureMapping()
                .put(TextureSlot.PARTICLE,
                        new Material(
                                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/tardis")
                        )
                );

        Identifier normalModel = Identifier.fromNamespaceAndPath(
                Constants.MOD_ID, "block/tardis_base"
        );

        Identifier flyingModel = TARDIS_FLYING.create(
                GCBlocks.TARDIS,
                tardisTextures,
                blockModels.modelOutput
        );

        registerBrachackiSet(blockModels);

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
        registerThorivine(blockModels);
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

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(GCBlocks.TARDIS)
                        .with(PropertyDispatch.initial(TardisBlock.IN_FLIGHT)
                                .select(false, BlockModelGenerators.plainVariant(normalModel))
                                .select(true, BlockModelGenerators.plainVariant(flyingModel)))
        );
        blockModels.createParticleOnlyBlock(GCBlocks.TARDIS_CONSOLE);
        blockModels.createParticleOnlyBlock(GCBlocks.TARDIS_TOP);
        blockModels.createParticleOnlyBlock(GCBlocks.TARDIS_INTERIOR_DOOR, GCBlocks.BRACHACKI_WALL_BLOCK);
        blockModels.createParticleOnlyBlock(GCBlocks.FIRST_DOCTORS_DOOR, GCBlocks.BRACHACKI_WALL_BLOCK);
        blockModels.createParticleOnlyBlock(GCBlocks.SECOND_DOCTORS_DOOR, GCBlocks.BRACHACKI_WALL_BLOCK);
        blockModels.createParticleOnlyBlock(GCBlocks.SECOND_DOCTORS_DOOR_ALT, GCBlocks.BRACHACKI_WALL_BLOCK);
        blockModels.createParticleOnlyBlock(GCBlocks.TUAT_DOOR, GCBlocks.BRACHACKI_WALL_BLOCK);
        blockModels.createParticleOnlyBlock(GCBlocks.INTERIOR_DOOR_BARRIER, GCBlocks.VOID_BLOCK);
        registerSelectableItems(itemModels);

        registerGrassBlock(blockModels);
        registerCadonwoodSet(blockModels);
        createCustomVine(blockModels, GCBlocks.GALLIFREYAN_VINE, GCBlocks.GALLIFREYAN_VINE.asItem());

        ItemModel.Unbaked tardisKey = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCItems.TARDIS_KEY, ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(GCItems.TARDIS_KEY, ItemModelUtils.select(new ComponentContents<>(GCDataComponents.KEY_STYLE), tardisKey,
                ItemModelUtils.when("gallifreyan", flatItem(itemModels, "gallifreyan_tardis_key")),
                ItemModelUtils.when("yale", flatItem(itemModels, "yale_tardis_key"))));
        ItemModel.Unbaked masterKey = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCItems.MASTER_TARDIS_KEY, ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(GCItems.MASTER_TARDIS_KEY, ItemModelUtils.select(new ComponentContents<>(GCDataComponents.KEY_STYLE), masterKey,
                ItemModelUtils.when("gallifreyan", flatItem(itemModels, "master_gallifreyan_tardis_key")),
                ItemModelUtils.when("yale", flatItem(itemModels, "master_yale_tardis_key"))));
        itemModels.generateFlatItem(GCItems.TARANIUM_SCRAP, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_CRYSTAL, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.REFINED_TARANIUM_CRYSTAL, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_INGOT, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.RAW_TIN, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.TARANIUM_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateTrimmableArmorSet(
                GCItems.TARANIUM_HELMET,
                GCItems.TARANIUM_CHESTPLATE,
                GCItems.TARANIUM_LEGGINGS,
                GCItems.TARANIUM_BOOTS,
                false,
                Map.of()
        );
    }

    private void registerSelectableItems(ItemModelGenerators itemModels) {
        ItemModel.Unbaked tardis = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCBlocks.TARDIS.asItem(), ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(GCBlocks.TARDIS.asItem(), ItemModelUtils.select(new TardisExteriorSelectProperty(), tardis,
                ItemModelUtils.when(Constants.MOD_ID + ":first_doctors_exterior", flatItem(itemModels, "first_doctors_tardis")),
                ItemModelUtils.when(Constants.MOD_ID + ":second_doctors_exterior", flatItem(itemModels, "second_doctors_tardis"))));

        ItemModel.Unbaked console = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCBlocks.TARDIS_CONSOLE.asItem(), ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(GCBlocks.TARDIS_CONSOLE.asItem(), ItemModelUtils.select(new ConsoleVariantSelectProperty(), console,
                ItemModelUtils.when(List.of("brachacki_console", "first_doctors_console"), console),
                ItemModelUtils.when("second_doctors_console", flatItem(itemModels, "tardis_console/second_doctors_console")),
                ItemModelUtils.when("second_doctors_console_alt", flatItem(itemModels, "tardis_console/second_doctors_console_alt")),
                ItemModelUtils.when("tuat_console", flatItem(itemModels, "tardis_console/tuat_console")),
                ItemModelUtils.when("inferno_console", flatItem(itemModels, "tardis_console/inferno_console")),
                ItemModelUtils.when("ambassadors_of_death_console", flatItem(itemModels, "tardis_console/ambassadors_of_death_console"))));

        ItemModel.Unbaked firstDoors = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCBlocks.FIRST_DOCTORS_DOOR.asItem(), ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked secondDoors = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCBlocks.SECOND_DOCTORS_DOOR.asItem(), ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked tuatDoors = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCBlocks.TUAT_DOOR.asItem(), ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked secondDoorsAlt = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCBlocks.SECOND_DOCTORS_DOOR_ALT.asItem(), ModelTemplates.FLAT_ITEM));
        ItemModel.Unbaked firstInteriorDoors = flatItem(itemModels, "interior_door/first_doctors_interior_doors");
        ItemModel.Unbaked secondInteriorDoors = flatItem(itemModels, "interior_door/second_doctors_interior_doors");
        itemModels.itemModelOutput.accept(GCBlocks.TARDIS_INTERIOR_DOOR.asItem(),
                ItemModelUtils.select(new ComponentContents<>(GCDataComponents.DOOR_TYPE), firstInteriorDoors,
                        ItemModelUtils.when("first_doctors_interior_doors", firstInteriorDoors),
                        ItemModelUtils.when("second_doctors_interior_doors", secondInteriorDoors)));
        Map<Item, ItemModel.Unbaked> doorFallbacks = Map.of(
                GCBlocks.FIRST_DOCTORS_DOOR.asItem(), firstDoors,
                GCBlocks.SECOND_DOCTORS_DOOR.asItem(), secondDoors,
                GCBlocks.SECOND_DOCTORS_DOOR_ALT.asItem(), secondDoorsAlt,
                GCBlocks.TUAT_DOOR.asItem(), tuatDoors);
        doorFallbacks.forEach((item, fallback) -> itemModels.itemModelOutput.accept(item,
                ItemModelUtils.select(new ComponentContents<>(GCDataComponents.DOOR_TYPE), fallback,
                        ItemModelUtils.when("first_doctors_door", firstDoors),
                        ItemModelUtils.when("second_doctors_door", secondDoors),
                        ItemModelUtils.when("second_doctors_door_alt", secondDoorsAlt),
                        ItemModelUtils.when("tuat_door", tuatDoors))));
    }

    private static ItemModel.Unbaked flatItem(ItemModelGenerators itemModels, String path) {
        Identifier model = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "item/" + path);
        ModelTemplates.FLAT_ITEM.create(model, TextureMapping.layer0(new Material(model)), itemModels.modelOutput);
        return ItemModelUtils.plainModel(model);
    }

    public void createCustomVine(BlockModelGenerators blockModelGenerators, Block vineBlock, Item vineItem) {
        blockModelGenerators.createMultifaceBlockStates(vineBlock);
        blockModelGenerators.registerSimpleItemModel(vineItem,
                blockModelGenerators.createFlatItemModelWithBlockTexture(vineItem, vineBlock));
//        this.registerSimpleTintedItemModel(Blocks.VINE, itemModel, ItemModelUtils.constantTint(-12012264));
    }

    private void registerBrachackiSet(BlockModelGenerators generators) {
        generators.createTrivialCube(GCBlocks.BRACHACKI_FLOOR_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_ROUNDEL_BLOCK);
        GCBlocks.WOOD_ROUNDELS.values().forEach(generators::createTrivialCube);
        GCBlocks.WOOD_HALF_ROUNDELS.values().forEach(generators::createTrivialCube);
        GCBlocks.CONCRETE_ROUNDELS.values().forEach(generators::createTrivialCube);
        GCBlocks.CONCRETE_HALF_ROUNDELS.values().forEach(generators::createTrivialCube);
        generators.createTrivialCube(GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_ROUNDEL_ALT_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_HALF_ROUNDEL_ALT_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_ROUNDEL_BLOCK_2);
        generators.createTrivialCube(GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_2);
        generators.createTrivialCube(GCBlocks.BRACHACKI_ROUNDEL_BLOCK_3);
        generators.createTrivialCube(GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_3);
        generators.createTrivialCube(GCBlocks.BRACHACKI_TUAT_FLOOR_BLOCK);
        generators.createTrivialCube(GCBlocks.VOID_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_TUAT_ROUNDEL_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_TUAT_HALF_ROUNDEL_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_TUAT_HEXAGONAL_ROUNDEL_BLOCK);
        generators.createTrivialCube(GCBlocks.BRACHACKI_TUAT_HALF_HEXAGONAL_ROUNDEL_BLOCK);

        generators.family(GCBlocks.BRACHACKI_WALL_BLOCK_2)
                .stairs(GCBlocks.BRACHACKI_STAIRS_2)
                .slab(GCBlocks.BRACHACKI_SLAB_2)
                .pressurePlate(GCBlocks.BRACHACKI_PRESSURE_PLATE_2)
                .wall(GCBlocks.BRACHACKI_WALL_2)
                .fence(GCBlocks.BRACHACKI_FENCE_2)
                .fenceGate(GCBlocks.BRACHACKI_FENCE_GATE_2);

        generators.family(GCBlocks.BRACHACKI_WALL_BLOCK_3)
                .stairs(GCBlocks.BRACHACKI_STAIRS_3)
                .slab(GCBlocks.BRACHACKI_SLAB_3)
                .pressurePlate(GCBlocks.BRACHACKI_PRESSURE_PLATE_3)
                .wall(GCBlocks.BRACHACKI_WALL_3)
                .fence(GCBlocks.BRACHACKI_FENCE_3)
                .fenceGate(GCBlocks.BRACHACKI_FENCE_GATE_3);

        generators.family(GCBlocks.BRACHACKI_TUAT_WALL_BLOCK)
                .stairs(GCBlocks.BRACHACKI_TUAT_STAIRS)
                .slab(GCBlocks.BRACHACKI_TUAT_SLAB)
                .pressurePlate(GCBlocks.BRACHACKI_TUAT_PRESSURE_PLATE)
                .wall(GCBlocks.BRACHACKI_TUAT_WALL)
                .fence(GCBlocks.BRACHACKI_TUAT_FENCE)
                .fenceGate(GCBlocks.BRACHACKI_TUAT_FENCE_GATE);

        generators.family(GCBlocks.BRACHACKI_WALL_BLOCK)
                .stairs(GCBlocks.BRACHACKI_STAIRS)
                .slab(GCBlocks.BRACHACKI_SLAB)
                .pressurePlate(GCBlocks.BRACHACKI_PRESSURE_PLATE)
                .wall(GCBlocks.BRACHACKI_WALL)
                .fence(GCBlocks.BRACHACKI_FENCE)
                .fenceGate(GCBlocks.BRACHACKI_FENCE_GATE);
    }


    private void registerThorivine(BlockModelGenerators generators) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(GCBlocks.THORIVINE, "_bottom"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(GCBlocks.THORIVINE, "_side"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(GCBlocks.THORIVINE, "_bottom"));

        Identifier model = THORIVINE_TEMPLATE.create(GCBlocks.THORIVINE, mapping, generators.modelOutput);

        generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                GCBlocks.THORIVINE,
                BlockModelGenerators.plainVariant(model)
        ));
        generators.registerSimpleItemModel(GCBlocks.THORIVINE, model);
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