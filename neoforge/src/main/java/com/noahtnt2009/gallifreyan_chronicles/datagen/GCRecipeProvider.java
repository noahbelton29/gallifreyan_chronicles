package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class GCRecipeProvider extends RecipeProvider {
    public GCRecipeProvider(BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        super(recipes, advancements);
    }

    public static MultiRegistryBootstrap bootstrap() {
        return RecipeProvider.asBootstrap(GCRecipeProvider::new);
    }

    @Override
    protected void buildRecipes() {
        List<ItemLike> TARANIUM_SMELTABLES = List.of(GCItems.TARANIUM_SCRAP,
                GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE);
        List<ItemLike> REFINED_TARANIUM_SMELTABLES = List.of(GCItems.TARANIUM_CRYSTAL);

        oreSmelting(TARANIUM_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, GCItems.TARANIUM_SCRAP,
                0.25f, 200, "taranium");
        oreBlasting(TARANIUM_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, GCItems.TARANIUM_SCRAP,
                0.25f, 100, "taranium");
        oreSmelting(REFINED_TARANIUM_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, GCItems.REFINED_TARANIUM_CRYSTAL,
                0.25f, 200, "taranium");
        oreBlasting(REFINED_TARANIUM_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, GCItems.REFINED_TARANIUM_CRYSTAL,
                0.25f, 100, "taranium");

        shaped(RecipeCategory.MISC, GCItems.TARANIUM_INGOT)
                .pattern("SSS")
                .pattern("SRR")
                .pattern("DDK")
                .define('S', GCItems.TARANIUM_SCRAP)
                .define('R', GCItems.REFINED_TARANIUM_CRYSTAL)
                .define('D', Items.DIAMOND)
                .define('K', Blocks.SCULK)
                .unlockedBy(getHasName(GCItems.TARANIUM_SCRAP), has(GCItems.REFINED_TARANIUM_CRYSTAL))
                .save(output);

        shaped(RecipeCategory.BUILDING_BLOCKS, GCBlocks.CADONWOOD_PLANKS, 4)
                .pattern("L")
                .define('L', GCBlocks.CADONWOOD_LOG)
                .unlockedBy(getHasName(GCBlocks.CADONWOOD_LOG), has(GCBlocks.CADONWOOD_LOG))
                .save(output);

        shaped(RecipeCategory.BUILDING_BLOCKS, GCBlocks.CADONWOOD_WOOD, 3)
                .pattern("LL")
                .pattern("LL")
                .define('L', GCBlocks.CADONWOOD_LOG)
                .unlockedBy(getHasName(GCBlocks.CADONWOOD_LOG), has(GCBlocks.CADONWOOD_LOG))
                .save(output);

        shaped(RecipeCategory.BUILDING_BLOCKS, GCBlocks.STRIPPED_CADONWOOD_WOOD, 3)
                .pattern("LL")
                .pattern("LL")
                .define('L', GCBlocks.STRIPPED_CADONWOOD_LOG)
                .unlockedBy(getHasName(GCBlocks.STRIPPED_CADONWOOD_LOG), has(GCBlocks.STRIPPED_CADONWOOD_LOG))
                .save(output);

        shaped(RecipeCategory.BUILDING_BLOCKS, GCBlocks.CADONWOOD_STAIRS, 4)
                .pattern("P  ")
                .pattern("PP ")
                .pattern("PPP")
                .define('P', GCBlocks.CADONWOOD_PLANKS)
                .unlockedBy(getHasName(GCBlocks.CADONWOOD_PLANKS), has(GCBlocks.CADONWOOD_PLANKS))
                .save(output);

        slab(RecipeCategory.BUILDING_BLOCKS, GCBlocks.CADONWOOD_SLAB, GCBlocks.CADONWOOD_PLANKS);

        shaped(RecipeCategory.DECORATIONS, GCBlocks.CADONWOOD_FENCE, 3)
                .pattern("PSP")
                .pattern("PSP")
                .define('P', GCBlocks.CADONWOOD_PLANKS)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCBlocks.CADONWOOD_PLANKS), has(GCBlocks.CADONWOOD_PLANKS))
                .save(output);

        shaped(RecipeCategory.DECORATIONS, GCBlocks.CADONWOOD_FENCE_GATE)
                .pattern("SPS")
                .pattern("SPS")
                .define('P', GCBlocks.CADONWOOD_PLANKS)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCBlocks.CADONWOOD_PLANKS), has(GCBlocks.CADONWOOD_PLANKS))
                .save(output);

        shaped(RecipeCategory.DECORATIONS, GCBlocks.CADONWOOD_WALL, 6)
                .pattern("PPP")
                .pattern("PPP")
                .define('P', GCBlocks.CADONWOOD_PLANKS)
                .unlockedBy(getHasName(GCBlocks.CADONWOOD_PLANKS), has(GCBlocks.CADONWOOD_PLANKS))
                .save(output);

        pressurePlate(GCBlocks.CADONWOOD_PRESSURE_PLATE, GCBlocks.CADONWOOD_PLANKS);

        buttonBuilder(GCBlocks.CADONWOOD_BUTTON, Ingredient.of(GCBlocks.CADONWOOD_PLANKS))
                .unlockedBy(getHasName(GCBlocks.CADONWOOD_PLANKS), has(GCBlocks.CADONWOOD_PLANKS))
                .save(output);

        shaped(RecipeCategory.COMBAT, GCItems.TARANIUM_SWORD)
                .pattern("T")
                .pattern("T")
                .pattern("S")
                .define('T', GCItems.TARANIUM_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.TOOLS, GCItems.TARANIUM_PICKAXE)
                .pattern("TTT")
                .pattern(" S ")
                .pattern(" S ")
                .define('T', GCItems.TARANIUM_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.TOOLS, GCItems.TARANIUM_AXE)
                .pattern("TT")
                .pattern("TS")
                .pattern(" S")
                .define('T', GCItems.TARANIUM_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.TOOLS, GCItems.TARANIUM_SHOVEL)
                .pattern("T")
                .pattern("S")
                .pattern("S")
                .define('T', GCItems.TARANIUM_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.TOOLS, GCItems.TARANIUM_HOE)
                .pattern("TT")
                .pattern(" S")
                .pattern(" S")
                .define('T', GCItems.TARANIUM_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.TOOLS, Items.STONE_PICKAXE)
                .pattern("GGG")
                .pattern(" S ")
                .pattern(" S ")
                .define('G', GCBlocks.GALLIFREYAN_COBBLESTONE)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(GCBlocks.GALLIFREYAN_COBBLESTONE), has(Items.STICK))
                .save(this.output, ResourceKey.create(
                        Registries.RECIPE,
                        GCUtils.modIdentifier("stone_pickaxe_from_gallifreyan_stone")));

        shaped(RecipeCategory.COMBAT, GCItems.TARANIUM_HELMET)
                .pattern("TTT")
                .pattern("T T")
                .define('T', GCItems.TARANIUM_INGOT)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.COMBAT, GCItems.TARANIUM_CHESTPLATE)
                .pattern("T T")
                .pattern("TTT")
                .pattern("TTT")
                .define('T', GCItems.TARANIUM_INGOT)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.COMBAT, GCItems.TARANIUM_LEGGINGS)
                .pattern("TTT")
                .pattern("T T")
                .pattern("T T")
                .define('T', GCItems.TARANIUM_INGOT)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);

        shaped(RecipeCategory.COMBAT, GCItems.TARANIUM_BOOTS)
                .pattern("T T")
                .pattern("T T")
                .define('T', GCItems.TARANIUM_INGOT)
                .unlockedBy(getHasName(GCItems.TARANIUM_INGOT), has(GCItems.TARANIUM_INGOT))
                .save(output);
    }

    @Override
    protected <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.@NonNull Factory<T> factory, List<ItemLike> smeltables,
                                                                @NonNull RecipeCategory craftingCategory, @NonNull CookingBookCategory cookingCategory,
                                                                @NonNull ItemLike result, float experience, int cookingTime, @NonNull String group, @NonNull String fromDesc) {
        for (ItemLike item : smeltables) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(item), craftingCategory, cookingCategory, result, experience, cookingTime, factory)
                    .group(group)
                    .unlockedBy(getHasName(item), this.has(item))
                    .save(this.output, Constants.MOD_ID + ":" + getItemName(result) + fromDesc + "_" + getItemName(item));
        }
    }
}