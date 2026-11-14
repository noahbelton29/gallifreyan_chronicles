package uk.nbforge.gallifreyan_chronicles.datagen.assets;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.item.SelectItemModel;


import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.client.model.ConsoleVariantSelectProperty;
import uk.nbforge.gallifreyan_chronicles.client.model.TardisExteriorSelectProperty;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorBlockJson;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;
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
        /* every door block just needs a particle, the wall it's set in */
        Map<String, String> doorParticles = new HashMap<>();
        for (DoorBlockJson.Entry door : DoorBlockJson.entries()) doorParticles.put(door.block(), door.particle());
        for (Block door : GCBlocks.interiorDoorBlocks()) {
            String particle = doorParticles.get(BuiltInRegistries.BLOCK.getKey(door).getPath());
            Block particleBlock = particle != null
                    ? BuiltInRegistries.BLOCK.getValue(Identifier.parse(particle))
                    : GCBlocks.BRACHACKI_WALL_BLOCK;
            blockModels.createParticleOnlyBlock(door, particleBlock);
        }
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
        itemModels.generateFlatItem(GCItems.MALLET, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(GCItems.STATTENHEIM_REMOTE, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(GCItems.JELLY_BABIES, ModelTemplates.FLAT_ITEM);
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
                ItemModelUtils.when(Constants.MOD_ID + ":second_doctors_exterior", flatItem(itemModels, "second_doctors_tardis")),
                ItemModelUtils.when(Constants.MOD_ID + ":tuat_exterior", flatItem(itemModels, "tuat_tardis"))));

        ItemModel.Unbaked console = ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCBlocks.TARDIS_CONSOLE.asItem(), ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(GCBlocks.TARDIS_CONSOLE.asItem(), ItemModelUtils.select(new ConsoleVariantSelectProperty(), console,
                ItemModelUtils.when(List.of("brachacki_console", "first_doctors_console"), console),
                ItemModelUtils.when("second_doctors_console", flatItem(itemModels, "tardis_console/second_doctors_console")),
                ItemModelUtils.when("second_doctors_console_alt", flatItem(itemModels, "tardis_console/second_doctors_console_alt")),
                ItemModelUtils.when("tuat_console", flatItem(itemModels, "tardis_console/tuat_console")),
                ItemModelUtils.when("inferno_console", flatItem(itemModels, "tardis_console/inferno_console")),
                ItemModelUtils.when("ambassadors_of_death_console", flatItem(itemModels, "tardis_console/ambassadors_of_death_console"))));

        /* tardis interior doors: one item, a model per interior door variant in the data */
        List<String> interiorVariants = interiorDoorVariants();
        if (!interiorVariants.isEmpty()) {
            List<SelectItemModel.SwitchCase<String>> interiorCases = new ArrayList<>();
            ItemModel.Unbaked interiorFallback = null;
            for (String variant : interiorVariants) {
                ItemModel.Unbaked model = flatItem(itemModels, "interior_door/" + variant);
                if (interiorFallback == null) interiorFallback = model;
                interiorCases.add(ItemModelUtils.when(variant, model));
            }
            itemModels.itemModelOutput.accept(GCBlocks.TARDIS_INTERIOR_DOOR.asItem(),
                    ItemModelUtils.select(new ComponentContents<>(GCDataComponents.DOOR_TYPE), interiorFallback, interiorCases));
        }

        /* plain doors: every door item can show every door type */
        Map<Block, ItemModel.Unbaked> doorModels = new LinkedHashMap<>();
        for (Block door : GCBlocks.pickableDoors()) {
            doorModels.put(door, ItemModelUtils.plainModel(itemModels.createFlatItemModel(door.asItem(), ModelTemplates.FLAT_ITEM)));
        }
        List<SelectItemModel.SwitchCase<String>> doorCases = new ArrayList<>();
        doorModels.forEach((door, model) ->
                doorCases.add(ItemModelUtils.when(BuiltInRegistries.BLOCK.getKey(door).getPath(), model)));
        doorModels.forEach((door, fallback) -> itemModels.itemModelOutput.accept(door.asItem(),
                ItemModelUtils.select(new ComponentContents<>(GCDataComponents.DOOR_TYPE), fallback, doorCases)));
    }

    /* interior door variant ids, from the same codec-read files the game uses, in priority order */
    private static List<String> interiorDoorVariants() {
        return DoorBlockJson.bundledVariants().stream()
                .filter(TardisDoorVariant::interior)
                .map(TardisDoorVariant::localId)
                .toList();
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
        generators.createTrivialCube(GCBlocks.BRACHACKI_ROUNDEL_BLOCK_4);
        generators.createTrivialCube(GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_3);
        generators.createTrivialCube(GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_4);
        generators.createTrivialCube(GCBlocks.BRACHACKI_LIGHT_STRIP_BLOCK);
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
        generators.family(GCBlocks.BRACHACKI_WALL_BLOCK_4)
                .stairs(GCBlocks.BRACHACKI_STAIRS_4)
                .slab(GCBlocks.BRACHACKI_SLAB_4)
                .pressurePlate(GCBlocks.BRACHACKI_PRESSURE_PLATE_4)
                .wall(GCBlocks.BRACHACKI_WALL_4)
                .fence(GCBlocks.BRACHACKI_FENCE_4)
                .fenceGate(GCBlocks.BRACHACKI_FENCE_GATE_4);

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

    private static TextureMapping grassMapping(String sideSuffix) {
        return new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_DIRT))
                .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_GRASS_BLOCK, "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(GCBlocks.GALLIFREYAN_GRASS_BLOCK, sideSuffix));
    }

    private void registerGrassBlock(BlockModelGenerators generators) {
        TextureMapping normalMapping = grassMapping("_side");

        Identifier normalModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                normalMapping,
                generators.modelOutput
        );

        TextureMapping snowyMapping = grassMapping("_snow");

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