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
            if (block ==  GCBlocks.TARDIS) {
                add(block, "TARDIS");
            } else {
                String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
                String displayName = Arrays.stream(path.split("_"))
                        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                        .collect(Collectors.joining(" "));
                add(block, displayName);
            }
        }
        for (Item item : GCItems.ITEMS) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            String displayName = Arrays.stream(path.split("_"))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
            add(item, displayName);
        }

        // misc
        add("gamerule.category.gallifreyan_chronicles.gallifreyan_chronicles", "Gallifreyan Chronicles");
        add("gamerule.gallifreyan_chronicles.gc_daylight_cycle_affects_glow", "Daylight Cycle Affects Exterior Glow");

        // creative
        add("creativetab.gallifreyan_chronicles.tardis", "GC: TARDIS");
        add("creativetab.gallifreyan_chronicles.natural_blocks", "GC: Natural Blocks");
        add("creativetab.gallifreyan_chronicles.building_blocks", "GC: Building Blocks");
        add("creativetab.gallifreyan_chronicles.flora", "GC: Flora");
        add("creativetab.gallifreyan_chronicles.tools_and_armor", "GC: Tools & Armor");

        // menu
        add("menu.gallifreyan_chronicles.credits", "Credits");

        // commands
        add("command.gallifreyan_chronicles.invalid_id", "Invalid TARDIS ID: %s");
        add("command.gallifreyan_chronicles.unknown_exterior", "Unknown exterior: %s");
        add("command.gallifreyan_chronicles.no_tardis_id", "No TARDIS found with ID: %s");

        add("command.gallifreyan_chronicles.set_exterior", "Set exterior of %s to %s");
        add("command.gallifreyan_chronicles.set_console", "Set exterior of %s to %s");
        add("command.gallifreyan_chronicles.get_exterior", "Exterior of %s: %s");
        add("command.gallifreyan_chronicles.get_console", "Exterior of %s: %s");
        add("command.gallifreyan_chronicles.unlink_console", "Console unlinked from TARDIS ID: %s");
        add("command.gallifreyan_chronicles.link_console", "Console linked to TARDIS ID: %s");
        add("command.gallifreyan_chronicles.unlink_exterior", "Exterior unlinked from TARDIS ID: %s");
        add("command.gallifreyan_chronicles.link_exterior", "Exterior linked to TARDIS ID: %s");
        add("command.gallifreyan_chronicles.set_glow", "Set glow of %s to %s");
        add("command.gallifreyan_chronicles.no_console", "No Console found for Console ID: %s.");

        add("command.gallifreyan_chronicles.not_looking_at_exterior", "No Exterior found in viewport.");
        add("command.gallifreyan_chronicles.not_looking_at_console", "No Console found in viewport.");

        add("command.gallifreyan_chronicles.no_tardises", "No TARDISes found.");
        add("command.gallifreyan_chronicles.list_tardis", "%s | exterior: %s | pos: %s");
        add("command.gallifreyan_chronicles.list_exterior", "Exterior: %s");

        add("command.gallifreyan_chronicles.debug", "=== TARDIS Debug: %s ===");
        add("command.gallifreyan_chronicles.owner", "Owner: %s");
        add("command.gallifreyan_chronicles.exterior", "Exterior: %s");
        add("command.gallifreyan_chronicles.position", "Position: %s");
    }
}