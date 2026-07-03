package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class GCItemTagProvider extends ItemTagsProvider {
    public GCItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Constants.MOD_ID);
    }

    private void tagAdd(TagKey<Item> tag, Item... items) {
        TagAppender<Item> appender = tag(tag);
        for (Item i : items) {
            appender.add(BuiltInRegistries.ITEM.getResourceKey(i).orElseThrow());
        }
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tagAdd(GCTags.Items.TARANIUM_REPAIRABLE,
                GCItems.TARANIUM_INGOT);

        tagAdd(ItemTags.SWORDS,
                GCItems.TARANIUM_SWORD);
        tagAdd(ItemTags.PICKAXES,
                GCItems.TARANIUM_PICKAXE);
        tagAdd(ItemTags.AXES,
                GCItems.TARANIUM_AXE);
        tagAdd(ItemTags.SHOVELS,
                GCItems.TARANIUM_SHOVEL);
        tagAdd(ItemTags.HOES,
                GCItems.TARANIUM_HOE);

        tagAdd(ItemTags.HEAD_ARMOR,
                GCItems.TARANIUM_HELMET);
        tagAdd(ItemTags.CHEST_ARMOR,
                GCItems.TARANIUM_CHESTPLATE);
        tagAdd(ItemTags.LEG_ARMOR,
                GCItems.TARANIUM_LEGGINGS);
        tagAdd(ItemTags.FOOT_ARMOR,
                GCItems.TARANIUM_BOOTS);

        tagAdd(ItemTags.TRIMMABLE_ARMOR,
                GCItems.TARANIUM_HELMET,
                GCItems.TARANIUM_CHESTPLATE,
                GCItems.TARANIUM_LEGGINGS,
                GCItems.TARANIUM_BOOTS);
    }
}
