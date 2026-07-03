package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GCLanguageProvider extends LanguageProvider {
    public GCLanguageProvider(PackOutput output) {
        super(output, Constants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (Block block : GCBlocks.BLOCKS) {
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
            String displayName = Arrays.stream(path.split("_"))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
            add(block, displayName);
        }
        for (Item item : GCItems.ITEMS) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            String displayName = Arrays.stream(path.split("_"))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
            add(item, displayName);
        }

        // misc
            //creative
        add("creativetab.gallifreyan_chronicles.natural_blocks", "GC: Natural Blocks");
        add("creativetab.gallifreyan_chronicles.building_blocks", "GC: Building Blocks");
        add("creativetab.gallifreyan_chronicles.flora", "GC: Flora");
        add("creativetab.gallifreyan_chronicles.tools_and_armor", "GC: Tools & Armor");
            //menu
        add("menu.gallifreyan_chronicles.credits", "Credits");
            //commands
        add("command.gallifreyan_chronicles.invalid_id","Invalid TARDIS ID:");
        add("command.gallifreyan_chronicles.no_tardis_id","No TARDIS found with ID:");
        add("command.gallifreyan_chronicles.debug","=== TARDIS Debug:");
        add("command.gallifreyan_chronicles.owner","Owner:    ");
        add("command.gallifreyan_chronicles.exterior","Exterior:    ");
        add("command.gallifreyan_chronicles.position","Position:    ");


    }
}