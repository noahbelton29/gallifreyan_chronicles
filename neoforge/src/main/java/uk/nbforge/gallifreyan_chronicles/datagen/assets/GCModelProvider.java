package uk.nbforge.gallifreyan_chronicles.datagen.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.client.model.ConsoleVariantSelectProperty;
import uk.nbforge.gallifreyan_chronicles.client.model.TardisExteriorSelectProperty;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.CadonwoodBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.FloraBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.GallifreyBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.InteriorBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.MoonBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.RoundelBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorBlockJson;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;

public class GCModelProvider extends ModelProvider {
    private static final int GALLIFREYAN_GRASS_TINT = 0xFFE0E0E0;

    private static final ModelTemplate THORIVINE_TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/thorivine_base")),
            Optional.empty(),
            TextureSlot.TOP,
            TextureSlot.SIDE,
            TextureSlot.BOTTOM);

    private static final ModelTemplate TARDIS_FLYING = new ModelTemplate(
                    Optional.of(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/tardis_base")),
                    Optional.of("_flying"),
                    TextureSlot.PARTICLE)
            .extend()
            .ambientOcclusion(false)
            .build();

    public GCModelProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(MoonBlocks.MOONSLATE);
        blockModels.createTrivialCube(MoonBlocks.MOON_DUST);
        blockModels.createTrivialCube(MoonBlocks.LUNAR_REGOLITH);

        TextureMapping tardisTextures = new TextureMapping()
                .put(
                        TextureSlot.PARTICLE,
                        new Material(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/tardis")));

        Identifier normalModel = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/tardis_base");

        Identifier flyingModel = TARDIS_FLYING.create(TardisBlocks.TARDIS, tardisTextures, blockModels.modelOutput);

        registerBrachackiSet(blockModels);

        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_DIRT);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_SAND);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_STONE);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_COBBLESTONE);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_GRAVEL);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_MUD);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_MUDSTONE);
        blockModels.createTrivialCube(GallifreyBlocks.ARCADIAN_SHALE);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_STONE_IRON_ORE);
        blockModels.createTrivialCube(GallifreyBlocks.GALLIFREYAN_STONE_COAL_ORE);
        blockModels.createTrivialCube(GallifreyBlocks.COBBLED_LIMESTONE);
        blockModels.createTrivialCube(GallifreyBlocks.ARCADIAN_SHALE_TARANIUM_ORE);
        registerThorivine(blockModels);
        blockModels.createCrossBlockWithDefaultItem(
                FloraBlocks.THORIVINE_FLOWER, BlockModelGenerators.PlantType.NOT_TINTED);

        blockModels.createCrossBlockWithDefaultItem(
                FloraBlocks.SHORT_GALLIFREYAN_DRY_GRASS, BlockModelGenerators.PlantType.NOT_TINTED);

        blockModels.createCrossBlockWithDefaultItem(
                FloraBlocks.TALL_GALLIFREYAN_DRY_GRASS, BlockModelGenerators.PlantType.NOT_TINTED);

        blockModels.createDoublePlant(FloraBlocks.TALL_GALLIFREYAN_GRASS, BlockModelGenerators.PlantType.TINTED);

        blockModels.registerSimpleTintedItemModel(
                FloraBlocks.TALL_GALLIFREYAN_GRASS,
                ModelLocationUtils.getModelLocation(FloraBlocks.TALL_GALLIFREYAN_GRASS, "_top"),
                ItemModelUtils.constantTint(GALLIFREYAN_GRASS_TINT));

        blockModels.createCrossBlock(FloraBlocks.SHORT_GALLIFREYAN_GRASS, BlockModelGenerators.PlantType.TINTED);

        Identifier itemModel = blockModels.createFlatItemModelWithBlockTexture(
                FloraBlocks.SHORT_GALLIFREYAN_GRASS.asItem(), FloraBlocks.SHORT_GALLIFREYAN_GRASS);

        blockModels.registerSimpleTintedItemModel(
                FloraBlocks.SHORT_GALLIFREYAN_GRASS, itemModel, ItemModelUtils.constantTint(GALLIFREYAN_GRASS_TINT));

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(TardisBlocks.TARDIS)
                .with(PropertyDispatch.initial(TardisBlock.IN_FLIGHT)
                        .select(false, BlockModelGenerators.plainVariant(normalModel))
                        .select(true, BlockModelGenerators.plainVariant(flyingModel))));
        blockModels.createParticleOnlyBlock(TardisBlocks.TARDIS_CONSOLE);
        blockModels.createParticleOnlyBlock(TardisBlocks.TARDIS_TOP);
        Map<String, String> doorParticles = new HashMap<>();
        for (DoorBlockJson.Entry door : DoorBlockJson.entries()) doorParticles.put(door.block(), door.particle());
        for (Block door : GCBlocks.interiorDoorBlocks()) {
            String particle =
                    doorParticles.get(BuiltInRegistries.BLOCK.getKey(door).getPath());
            Block particleBlock = particle != null
                    ? BuiltInRegistries.BLOCK.getValue(Identifier.parse(particle))
                    : InteriorBlocks.BRACHACKI_WALL_BLOCK;
            blockModels.createParticleOnlyBlock(door, particleBlock);
        }
        blockModels.createParticleOnlyBlock(TardisBlocks.INTERIOR_DOOR_BARRIER, InteriorBlocks.VOID_BLOCK);
        registerSelectableItems(itemModels);

        registerGrassBlock(blockModels);
        registerCadonwoodSet(blockModels);
        createCustomVine(blockModels, FloraBlocks.GALLIFREYAN_VINE, FloraBlocks.GALLIFREYAN_VINE.asItem());

        ItemModel.Unbaked tardisKey =
                ItemModelUtils.plainModel(itemModels.createFlatItemModel(GCItems.TARDIS_KEY, ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(
                GCItems.TARDIS_KEY,
                ItemModelUtils.select(
                        new ComponentContents<>(GCDataComponents.KEY_STYLE),
                        tardisKey,
                        ItemModelUtils.when("gallifreyan", flatItem(itemModels, "gallifreyan_tardis_key")),
                        ItemModelUtils.when("yale", flatItem(itemModels, "yale_tardis_key"))));
        ItemModel.Unbaked masterKey = ItemModelUtils.plainModel(
                itemModels.createFlatItemModel(GCItems.MASTER_TARDIS_KEY, ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(
                GCItems.MASTER_TARDIS_KEY,
                ItemModelUtils.select(
                        new ComponentContents<>(GCDataComponents.KEY_STYLE),
                        masterKey,
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
                Map.of());
    }

    private void registerSelectableItems(ItemModelGenerators itemModels) {
        ItemModel.Unbaked tardis = ItemModelUtils.plainModel(
                itemModels.createFlatItemModel(TardisBlocks.TARDIS.asItem(), ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(
                TardisBlocks.TARDIS.asItem(),
                ItemModelUtils.select(
                        new TardisExteriorSelectProperty(),
                        tardis,
                        ItemModelUtils.when(
                                Constants.MOD_ID + ":first_doctors_exterior",
                                flatItem(itemModels, "first_doctors_tardis")),
                        ItemModelUtils.when(
                                Constants.MOD_ID + ":second_doctors_exterior",
                                flatItem(itemModels, "second_doctors_tardis")),
                        ItemModelUtils.when(Constants.MOD_ID + ":tuat_exterior", flatItem(itemModels, "tuat_tardis"))));

        ItemModel.Unbaked console = ItemModelUtils.plainModel(
                itemModels.createFlatItemModel(TardisBlocks.TARDIS_CONSOLE.asItem(), ModelTemplates.FLAT_ITEM));
        itemModels.itemModelOutput.accept(
                TardisBlocks.TARDIS_CONSOLE.asItem(),
                ItemModelUtils.select(
                        new ConsoleVariantSelectProperty(),
                        console,
                        ItemModelUtils.when(List.of("brachacki_console", "first_doctors_console"), console),
                        ItemModelUtils.when(
                                "second_doctors_console",
                                flatItem(itemModels, "tardis_console/second_doctors_console")),
                        ItemModelUtils.when(
                                "second_doctors_console_alt",
                                flatItem(itemModels, "tardis_console/second_doctors_console_alt")),
                        ItemModelUtils.when("tuat_console", flatItem(itemModels, "tardis_console/tuat_console")),
                        ItemModelUtils.when("inferno_console", flatItem(itemModels, "tardis_console/inferno_console")),
                        ItemModelUtils.when(
                                "ambassadors_of_death_console",
                                flatItem(itemModels, "tardis_console/ambassadors_of_death_console"))));

        List<String> interiorVariants = interiorDoorVariants();
        if (!interiorVariants.isEmpty()) {
            List<SelectItemModel.SwitchCase<String>> interiorCases = new ArrayList<>();
            ItemModel.Unbaked interiorFallback = null;
            for (String variant : interiorVariants) {
                ItemModel.Unbaked model = flatItem(itemModels, "interior_door/" + variant);
                if (interiorFallback == null) interiorFallback = model;
                interiorCases.add(ItemModelUtils.when(variant, model));
            }
            itemModels.itemModelOutput.accept(
                    TardisBlocks.TARDIS_INTERIOR_DOOR.asItem(),
                    ItemModelUtils.select(
                            new ComponentContents<>(GCDataComponents.DOOR_TYPE), interiorFallback, interiorCases));
        }

        Map<Block, ItemModel.Unbaked> doorModels = new LinkedHashMap<>();
        for (Block door : GCBlocks.pickableDoors()) {
            doorModels.put(
                    door,
                    ItemModelUtils.plainModel(itemModels.createFlatItemModel(door.asItem(), ModelTemplates.FLAT_ITEM)));
        }
        List<SelectItemModel.SwitchCase<String>> doorCases = new ArrayList<>();
        doorModels.forEach((door, model) -> doorCases.add(
                ItemModelUtils.when(BuiltInRegistries.BLOCK.getKey(door).getPath(), model)));
        doorModels.forEach((door, fallback) -> itemModels.itemModelOutput.accept(
                door.asItem(),
                ItemModelUtils.select(new ComponentContents<>(GCDataComponents.DOOR_TYPE), fallback, doorCases)));
    }

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
        blockModelGenerators.registerSimpleItemModel(
                vineItem, blockModelGenerators.createFlatItemModelWithBlockTexture(vineItem, vineBlock));
    }

    private void registerBrachackiSet(BlockModelGenerators generators) {
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_FLOOR_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK);
        RoundelBlocks.WOOD_ROUNDELS.values().forEach(generators::createTrivialCube);
        RoundelBlocks.WOOD_HALF_ROUNDELS.values().forEach(generators::createTrivialCube);
        RoundelBlocks.CONCRETE_ROUNDELS.values().forEach(generators::createTrivialCube);
        RoundelBlocks.CONCRETE_HALF_ROUNDELS.values().forEach(generators::createTrivialCube);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_ROUNDEL_ALT_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_ALT_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_2);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_2);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_3);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_4);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_3);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_4);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_LIGHT_STRIP_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_TUAT_FLOOR_BLOCK);
        generators.createTrivialCube(InteriorBlocks.VOID_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_TUAT_ROUNDEL_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_TUAT_HALF_ROUNDEL_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_TUAT_HEXAGONAL_ROUNDEL_BLOCK);
        generators.createTrivialCube(InteriorBlocks.BRACHACKI_TUAT_HALF_HEXAGONAL_ROUNDEL_BLOCK);

        generators
                .family(InteriorBlocks.BRACHACKI_WALL_BLOCK_2)
                .stairs(InteriorBlocks.BRACHACKI_STAIRS_2)
                .slab(InteriorBlocks.BRACHACKI_SLAB_2)
                .pressurePlate(InteriorBlocks.BRACHACKI_PRESSURE_PLATE_2)
                .wall(InteriorBlocks.BRACHACKI_WALL_2)
                .fence(InteriorBlocks.BRACHACKI_FENCE_2)
                .fenceGate(InteriorBlocks.BRACHACKI_FENCE_GATE_2);

        generators
                .family(InteriorBlocks.BRACHACKI_WALL_BLOCK_3)
                .stairs(InteriorBlocks.BRACHACKI_STAIRS_3)
                .slab(InteriorBlocks.BRACHACKI_SLAB_3)
                .pressurePlate(InteriorBlocks.BRACHACKI_PRESSURE_PLATE_3)
                .wall(InteriorBlocks.BRACHACKI_WALL_3)
                .fence(InteriorBlocks.BRACHACKI_FENCE_3)
                .fenceGate(InteriorBlocks.BRACHACKI_FENCE_GATE_3);
        generators
                .family(InteriorBlocks.BRACHACKI_WALL_BLOCK_4)
                .stairs(InteriorBlocks.BRACHACKI_STAIRS_4)
                .slab(InteriorBlocks.BRACHACKI_SLAB_4)
                .pressurePlate(InteriorBlocks.BRACHACKI_PRESSURE_PLATE_4)
                .wall(InteriorBlocks.BRACHACKI_WALL_4)
                .fence(InteriorBlocks.BRACHACKI_FENCE_4)
                .fenceGate(InteriorBlocks.BRACHACKI_FENCE_GATE_4);

        generators
                .family(InteriorBlocks.BRACHACKI_TUAT_WALL_BLOCK)
                .stairs(InteriorBlocks.BRACHACKI_TUAT_STAIRS)
                .slab(InteriorBlocks.BRACHACKI_TUAT_SLAB)
                .pressurePlate(InteriorBlocks.BRACHACKI_TUAT_PRESSURE_PLATE)
                .wall(InteriorBlocks.BRACHACKI_TUAT_WALL)
                .fence(InteriorBlocks.BRACHACKI_TUAT_FENCE)
                .fenceGate(InteriorBlocks.BRACHACKI_TUAT_FENCE_GATE);

        generators
                .family(InteriorBlocks.BRACHACKI_WALL_BLOCK)
                .stairs(InteriorBlocks.BRACHACKI_STAIRS)
                .slab(InteriorBlocks.BRACHACKI_SLAB)
                .pressurePlate(InteriorBlocks.BRACHACKI_PRESSURE_PLATE)
                .wall(InteriorBlocks.BRACHACKI_WALL)
                .fence(InteriorBlocks.BRACHACKI_FENCE)
                .fenceGate(InteriorBlocks.BRACHACKI_FENCE_GATE);
    }

    private void registerThorivine(BlockModelGenerators generators) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(FloraBlocks.THORIVINE, "_bottom"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(FloraBlocks.THORIVINE, "_side"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(FloraBlocks.THORIVINE, "_bottom"));

        Identifier model = THORIVINE_TEMPLATE.create(FloraBlocks.THORIVINE, mapping, generators.modelOutput);

        generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(
                FloraBlocks.THORIVINE, BlockModelGenerators.plainVariant(model)));
        generators.registerSimpleItemModel(FloraBlocks.THORIVINE, model);
    }

    private static TextureMapping grassMapping(String sideSuffix) {
        return new TextureMapping()
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(GallifreyBlocks.GALLIFREYAN_DIRT))
                .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK, "_top"))
                .put(
                        TextureSlot.SIDE,
                        TextureMapping.getBlockTexture(GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK, sideSuffix));
    }

    private void registerGrassBlock(BlockModelGenerators generators) {
        TextureMapping normalMapping = grassMapping("_side");

        Identifier normalModel = ModelTemplates.CUBE_BOTTOM_TOP.create(
                GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK, normalMapping, generators.modelOutput);

        TextureMapping snowyMapping = grassMapping("_snow");

        MultiVariant snowyVariant = BlockModelGenerators.plainVariant(ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(
                GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK, "_snow", snowyMapping, generators.modelOutput));

        generators.createGrassLikeBlock(
                GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK,
                BlockModelGenerators.createRotatedVariants(new Variant(normalModel)),
                snowyVariant);
    }

    private void registerCadonwoodSet(BlockModelGenerators generators) {
        generators
                .family(CadonwoodBlocks.CADONWOOD_PLANKS)
                .stairs(CadonwoodBlocks.CADONWOOD_STAIRS)
                .slab(CadonwoodBlocks.CADONWOOD_SLAB)
                .pressurePlate(CadonwoodBlocks.CADONWOOD_PRESSURE_PLATE)
                .button(CadonwoodBlocks.CADONWOOD_BUTTON)
                .fence(CadonwoodBlocks.CADONWOOD_FENCE)
                .fenceGate(CadonwoodBlocks.CADONWOOD_FENCE_GATE)
                .wall(CadonwoodBlocks.CADONWOOD_WALL);

        generators
                .woodProvider(CadonwoodBlocks.CADONWOOD_LOG)
                .log(CadonwoodBlocks.CADONWOOD_LOG)
                .wood(CadonwoodBlocks.CADONWOOD_WOOD);

        generators
                .woodProvider(CadonwoodBlocks.STRIPPED_CADONWOOD_LOG)
                .log(CadonwoodBlocks.STRIPPED_CADONWOOD_LOG)
                .wood(CadonwoodBlocks.STRIPPED_CADONWOOD_WOOD);

        generators.createTrivialBlock(CadonwoodBlocks.CADONWOOD_LEAVES, TexturedModel.LEAVES);

        generators.createPlantWithDefaultItem(
                CadonwoodBlocks.CADONWOOD_SAPLING,
                CadonwoodBlocks.POTTED_CADONWOOD_SAPLING,
                BlockModelGenerators.PlantType.TINTED);
    }
}
