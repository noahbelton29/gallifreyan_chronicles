package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class GCTags {
    public static class Blocks {
        public static final TagKey<Block> GALLIFREYAN_STONE_REPLACEABLES = createTag("gallifreyan_stone_replaceables");
        public static final TagKey<Block> BASE_STONE_GALLIFREY = createTag("base_stone_gallifrey");
        public static final TagKey<Block> INCORRECT_FOR_TARANIUM_TOOL = createTag("incorrect_for_taranium_tool");
        public static final TagKey<Block> NEEDS_NETHERITE_TOOL = createTag("needs_netherite_tool");

        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK, GCUtils.of(name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TARANIUM_REPAIRABLE = createTag("taranium_repairable");

        private static TagKey<Item> createTag(String name) {
            return TagKey.create(Registries.ITEM, GCUtils.of(name));
        }
    }
}
