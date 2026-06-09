package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class GCTags {
    public static class Blocks {
        public static final TagKey<Block> GALLIFREYAN_STONE_REPLACEABLES = createTag("gallifreyan_stone_replaceables");
        public static final TagKey<Block> BASE_STONE_GALLIFREY = createTag("base_stone_gallifrey");

        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name));
        }
    }

    public static class Items {
        private static TagKey<Item> createTag(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name));
        }
    }
}
