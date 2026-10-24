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
        // Blocks
        for (Block block : GCBlocks.BLOCKS) {
            if (block == GCBlocks.TARDIS) {
                add(block, "TARDIS");
            } else {
                String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
                String displayName = Arrays.stream(path.split("_"))
                        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                        .collect(Collectors.joining(" "));
                add(block, displayName);
            }
        }

        // Items
        for (Item item : GCItems.ITEMS) {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            String displayName = Arrays.stream(path.split("_"))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
            add(item, displayName);
        }

        // Miscellaneous
        add("gamerule.category.gallifreyan_chronicles.gallifreyan_chronicles",
                "Gallifreyan Chronicles");
        add("gamerule.gallifreyan_chronicles.gc_daylight_cycle_affects_glow",
                "Daylight Cycle Affects Exterior Glow");

        // Creative tabs
        add("creativetab.gallifreyan_chronicles.tardis", "GC: TARDIS");
        add("creativetab.gallifreyan_chronicles.natural_blocks", "GC: Natural Blocks");
        add("creativetab.gallifreyan_chronicles.building_blocks", "GC: Building Blocks");
        add("creativetab.gallifreyan_chronicles.flora", "GC: Flora");
        add("creativetab.gallifreyan_chronicles.tools_and_armor", "GC: Tools & Armor");

        // Menus
        add("menu.gallifreyan_chronicles.credits", "Credits");

        // Commands: General
        add("command.gallifreyan_chronicles.invalid_id",
                "Invalid TARDIS ID: %s");
        add("command.gallifreyan_chronicles.no_tardis_id",
                "No TARDIS found with ID: %s");

        // Commands: Exteriors
        add("command.gallifreyan_chronicles.unknown_exterior",
                "Unknown exterior: %s");
        add("command.gallifreyan_chronicles.set_exterior",
                "Set exterior of TARDIS %s to %s");
        add("command.gallifreyan_chronicles.get_exterior",
                "Exterior of TARDIS %s: %s");
        add("command.gallifreyan_chronicles.no_exterior",
                "No exterior found for TARDIS ID: %s");
        add("command.gallifreyan_chronicles.link_exterior",
                "Exterior linked to TARDIS ID: %s");
        add("command.gallifreyan_chronicles.unlink_exterior",
                "Exterior unlinked from TARDIS ID: %s");
        add("command.gallifreyan_chronicles.not_looking_at_exterior",
                "You are not looking at a TARDIS exterior.");

        // Commands: Interiors
        add("command.gallifreyan_chronicles.get_interior",
                "Interior of TARDIS %s: %s");
        add("command.gallifreyan_chronicles.set_interior",
                "Set interior of TARDIS %s to %s");
        add("command.gallifreyan_chronicles.unknown_interior",
                "Unknown interior: %s");
        add("command.gallifreyan_chronicles.missing_interior_template",
                "Missing template for interior: %s");
        add("command.gallifreyan_chronicles.interior_unchanged",
                "Interior of TARDIS %s is already set to %s");

        // Commands: Consoles
        add("command.gallifreyan_chronicles.unknown_console",
                "Unknown console: %s");
        add("command.gallifreyan_chronicles.set_console",
                "Set console of TARDIS %s to %s");
        add("command.gallifreyan_chronicles.get_console",
                "Console of TARDIS %s: %s");
        add("command.gallifreyan_chronicles.no_console",
                "No console found for TARDIS ID: %s");
        add("command.gallifreyan_chronicles.link_console",
                "Console linked to TARDIS ID: %s");
        add("command.gallifreyan_chronicles.unlink_console",
                "Console unlinked from TARDIS ID: %s");
        add("command.gallifreyan_chronicles.not_looking_at_console",
                "You are not looking at a TARDIS console.");

        // Commands: Console variants
        add("command.gallifreyan_chronicles.unknown_console_variant",
                "Unknown console variant '%s' for console '%s'");
        add("command.gallifreyan_chronicles.set_console_variant",
                "Set console variant of TARDIS %s to %s");

        // Commands: Interior door variants
        add("command.gallifreyan_chronicles.no_interior_door",
                "No interior doors found for TARDIS ID: %s");
        add("command.gallifreyan_chronicles.unknown_door_variant",
                "Unknown door variant '%s' for door type '%s'");
        add("command.gallifreyan_chronicles.set_door_variant",
                "Set door variant of TARDIS %s to %s (%s doors updated)");

        // Commands: Summon
        add("command.gallifreyan_chronicles.summon_tardis",
                "Summoned TARDIS %s to %s");
        add("command.gallifreyan_chronicles.summon_blocked",
                "Something is in the way, move and try again");
        add("command.gallifreyan_chronicles.player_only",
                "This command can only be used by a player");

        // Commands: Exterior glow
        add("command.gallifreyan_chronicles.set_glow",
                "Set exterior glow of TARDIS %s to %s");

        // Commands: Fade type
        add("command.gallifreyan_chronicles.get_fade_type",
                "TARDIS %s uses the %s fade");
        add("command.gallifreyan_chronicles.set_fade_type",
                "Set fade type of TARDIS %s to %s");
        add("command.gallifreyan_chronicles.unknown_fade_type",
                "Unknown fade type %s (available: %s)");

        // Commands: Subsystems
        add("command.gallifreyan_chronicles.unknown_subsystem",
                "Unknown TARDIS subsystem: %s");
        add("command.gallifreyan_chronicles.set_subsystem_health",
                "Set health of subsystem %s in TARDIS %s to %s");
        add("command.gallifreyan_chronicles.get_subsystem_health",
                "Health of subsystem %s in TARDIS %s: %s");

        // Commands: TARDIS listing
        add("command.gallifreyan_chronicles.no_tardises",
                "No TARDISes found.");
        add("command.gallifreyan_chronicles.list_tardis",
                "%s | Exterior: %s | Position: %s");

        // Commands: Debug
        add("command.gallifreyan_chronicles.debug",
                "=== TARDIS Debug: %s ===");
        add("command.gallifreyan_chronicles.owner",
                "Owner: %s");
        add("command.gallifreyan_chronicles.exterior",
                "Exterior: %s");
        add("command.gallifreyan_chronicles.position",
                "Position: %s");

        // Commands: Console controls
        add("command.gallifreyan_chronicles.no_control_entities",
                "This console has no control entities.");
        add("command.gallifreyan_chronicles.player_only",
                "This command must be run by a player.");
    }
}