package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
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

    private static String titleCase(String path) {
        return Arrays.stream(path.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    @Override
    protected void addTranslations() {
        // Blocks
        for (Block block : GCBlocks.BLOCKS) {
            if (block == GCBlocks.TARDIS) {
                add(block, "TARDIS");
            } else if (block == GCBlocks.TARDIS_CONSOLE) {
                add(block, "TARDIS Console");
            } else if (block == GCBlocks.TARDIS_INTERIOR_DOOR) {
                add(block, "TARDIS Interior Door");
            } else {
                add(block, titleCase(BuiltInRegistries.BLOCK.getKey(block).getPath()).replace("Tuat", "TUAT"));
            }
        }

        // Items
        for (Item item : GCItems.ITEMS) {
            if (item == GCItems.TARDIS_KEY) {
                add(item, "TARDIS Key");
                continue;
            }
            if (item == GCItems.MASTER_TARDIS_KEY) {
                add(item, "Master TARDIS Key");
                continue;
            }
            add(item, titleCase(BuiltInRegistries.ITEM.getKey(item).getPath()));
        }

        // TARDIS controls
        for (String id : TardisControlRegistry.allIds()) {
            add(TardisControlRegistry.translationKey(id), titleCase(id));
        }

        // Entities
        add("entity.gallifreyan_chronicles.tardis_control", "TARDIS Control");

        // Miscellaneous
        add("gamerule.category.gallifreyan_chronicles.gallifreyan_chronicles",
                "Gallifreyan Chronicles");
        add("gamerule.gallifreyan_chronicles.gc_daylight_cycle_affects_glow",
                "Daylight Cycle Affects Exterior Glow");

        // Creative tabs
        add("creativetab.gallifreyan_chronicles.tardis", "GC: TARDIS");
        add("tooltip.gallifreyan_chronicles.tardis.schema", "Schema");
        add("tooltip.gallifreyan_chronicles.console.model", "Console");
        add("tooltip.gallifreyan_chronicles.console.variant", "Variant");
        add("tooltip.gallifreyan_chronicles.console.variant_default", "Default");
        add("tooltip.gallifreyan_chronicles.door.type", "Door");
        add("door_type.gallifreyan_chronicles.first_doctors_door", "First Doctor's Door");
        add("door_type.gallifreyan_chronicles.second_doctors_door", "Second Doctor's Door");
        add("door_type.gallifreyan_chronicles.second_doctors_door_alt", "Second Doctor's Door (Alt)");
        add("door_type.gallifreyan_chronicles.tuat_door", "TUAT Door");
        add("door_type.gallifreyan_chronicles.first_doctors_interior_doors", "First Doctor's Interior Doors");
        add("door_type.gallifreyan_chronicles.second_doctors_interior_doors", "Second Doctor's Interior Doors");
        add("door_type.gallifreyan_chronicles.tuat_interior_doors", "TUAT Interior Doors");
        add("tooltip.gallifreyan_chronicles.scroll", "Scroll to change");
        add("tooltip.gallifreyan_chronicles.key.linked", "Linked to TARDIS ID");
        add("tooltip.gallifreyan_chronicles.key.master", "Works on every TARDIS");
        add("tooltip.gallifreyan_chronicles.key.no_trusted", "No trusted players");
        add("tooltip.gallifreyan_chronicles.key.trusted", "Trusted");
        add("tooltip.gallifreyan_chronicles.key.unlinked", "Not linked to a TARDIS");
        add("creativetab.gallifreyan_chronicles.natural_blocks", "GC: Natural Blocks");
        add("creativetab.gallifreyan_chronicles.building_blocks", "GC: Building Blocks");
        add("creativetab.gallifreyan_chronicles.flora", "GC: Flora");
        add("creativetab.gallifreyan_chronicles.tools_and_armor", "GC: Tools & Armor");

        // Menus
        add("menu.gallifreyan_chronicles.credits", "Credits");
        add("menu.gallifreyan_chronicles.game_version", "Minecraft %s %s");
        add("menu.gallifreyan_chronicles.mods", "Mods");
        add("menu.gallifreyan_chronicles.credits.coding", "Coding");
        add("menu.gallifreyan_chronicles.credits.disclaimer", "Disclaimer");
        add("menu.gallifreyan_chronicles.credits.disclaimer.ownership", "Doctor Who and the TARDIS belong to the BBC.");
        add("menu.gallifreyan_chronicles.credits.disclaimer.credit", "All credit for the Doctor Who assets goes to the BBC.");
        add("menu.gallifreyan_chronicles.credits.disclaimer.unofficial", "This is an unofficial fan project, not affiliated with or endorsed by the BBC.");
        add("menu.gallifreyan_chronicles.credits.interiors", "Interior Builds");
        add("menu.gallifreyan_chronicles.credits.models", "Assets/Models");
        add("menu.gallifreyan_chronicles.credits.special_thanks", "Special Thanks");
        add("menu.gallifreyan_chronicles.credits.suggestions", "Suggestions");
        add("menu.gallifreyan_chronicles.credits.textures", "Textures");
        add("menu.gallifreyan_chronicles.credits.title_theme", "Title Theme");
        add("menu.gallifreyan_chronicles.credits.texturing_tips", "Tips and Tricks on Texturing");
        add("menu.gallifreyan_chronicles.addons", "Addons");
        add("menu.gallifreyan_chronicles.options", "Options");
        add("menu.gallifreyan_chronicles.essential", "Essential");
        add("menu.gallifreyan_chronicles.essential.invite_host", "Host");
        add("menu.gallifreyan_chronicles.essential.social", "Social");
        add("menu.gallifreyan_chronicles.essential.wardrobe", "Wardrobe");
        add("menu.gallifreyan_chronicles.essential.pictures", "Pictures");
        add("menu.gallifreyan_chronicles.essential.settings", "Settings");
        add("menu.gallifreyan_chronicles.essential.account", "Account");
        add("menu.gallifreyan_chronicles.replay_viewer", "Replay Viewer");
        add("menu.gallifreyan_chronicles.game_options", "Game Options");
        add("menu.gallifreyan_chronicles.settings", "Mod Settings");
        add("menu.gallifreyan_chronicles.version", "Gallifreyan Chronicles v%s");

        // Commands: General
        add("command.gallifreyan_chronicles.invalid_id",
                "Invalid TARDIS ID: %s");
        add("command.gallifreyan_chronicles.no_tardis_id",
                "No TARDIS found with ID: %s");

        // Commands: Exteriors
        add("command.gallifreyan_chronicles.unknown_exterior",
                "Unknown exterior: %s");
        add("command.gallifreyan_chronicles.remove_tardis",
                "Removed TARDIS %s, its exterior and its interior dimension");
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
        add("command.gallifreyan_chronicles.unknown_control",
                "Unknown TARDIS control: %s");
        add("command.gallifreyan_chronicles.control_not_on_console",
                "Control %s is not on the console of TARDIS %s");
        add("command.gallifreyan_chronicles.control_already",
                "Control %s is already %s");
        add("command.gallifreyan_chronicles.control_refused",
                "Control %s refused to turn %s");
        add("command.gallifreyan_chronicles.set_control",
                "Turned control %s of TARDIS %s %s");

        // Commands: Destinations
        add("command.gallifreyan_chronicles.destination_bad_facing",
                "Unknown facing: %s");
        add("command.gallifreyan_chronicles.destination_interior",
                "A destination can't be inside a TARDIS interior");
        add("command.gallifreyan_chronicles.destination_out_of_world",
                "That position is outside the world");
        add("command.gallifreyan_chronicles.destination_set",
                "Set destination of TARDIS %s to %s in %s facing %s");
        add("command.gallifreyan_chronicles.destination_get",
                "Destination of TARDIS %s: %s in %s facing %s");
        add("command.gallifreyan_chronicles.destination_none",
                "TARDIS %s has no destination set");
        add("command.gallifreyan_chronicles.destination_cleared",
                "Cleared destination of TARDIS %s");

        // Commands: Debug listing
        add("command.gallifreyan_chronicles.list_exterior",
                "Exteriors: %s");

        // Overlays
        add("overlay.gallifreyan_chronicles.destination",
                "Destination: %s  |  %s");
        add("overlay.gallifreyan_chronicles.destination_dimension",
                "Dimension: %s  (%s)");
        add("overlay.gallifreyan_chronicles.coord_increment",
                "Increment: %s");
        add("overlay.gallifreyan_chronicles.destination_direction",
                "Facing: %s");

        // Messages
        add("message.gallifreyan_chronicles.lock_too_damaged", "The lock is too damaged to take the key!");
        add("message.gallifreyan_chronicles.lock_jammed", "The lock is jammed!");
        add("message.gallifreyan_chronicles.wrong_key", "That is the wrong key!");
        add("message.gallifreyan_chronicles.key_lockout", "That key is jammed in this lock. Try again in %s s.");
        add("message.gallifreyan_chronicles.locked", "Locked, this TARDIS");
        add("message.gallifreyan_chronicles.locked_owned", "Locked, this TARDIS belongs to %s");
        add("message.gallifreyan_chronicles.unknown_player", "an unknown Player");
        add("message.gallifreyan_chronicles.not_your_key", "That is not your key!");
        add("message.gallifreyan_chronicles.close_doors_to_remove_key", "Close the doors before removing the key!");
        add("message.gallifreyan_chronicles.close_doors_to_insert_key", "Close both doors before inserting the key!");
        add("message.gallifreyan_chronicles.trust_added", "%s can now use your TARDIS controls.");
        add("message.gallifreyan_chronicles.trust_already", "%s is already trusted.");
        add("message.gallifreyan_chronicles.trust_granted", "%s trusted you with their TARDIS controls.");
        add("message.gallifreyan_chronicles.trust_not_trusted", "%s isn't trusted.");
        add("message.gallifreyan_chronicles.trust_not_yours", "Only the owner of this TARDIS can trust people.");
        add("message.gallifreyan_chronicles.trust_removed", "%s can no longer use your TARDIS controls.");
        add("message.gallifreyan_chronicles.trust_revoked", "%s revoked your access to their TARDIS controls.");
        add("gc.build.expired.title", "Test build expired");
        add("gc.build.expires_in", "Build expires in %s");
        add("gc.build.expired.message", "This test build of Gallifreyan Chronicles has expired. Ask for a new build to keep testing.");
        add("options.gallifreyan_chronicles.tardis_hum", "TARDIS Hum");
        add("options.gallifreyan_chronicles.camera_shake", "TARDIS Camera Shake");
        add("options.gallifreyan_chronicles.camera_shake.tooltip", "How strongly the camera shakes during TARDIS takeoff, flight and landing.");
        add("addons.gallifreyan_chronicles.title", "Addons");
        add("addons.gallifreyan_chronicles.drop_hint", "Drag addon .zip or .jar files into this window to install them.");
        add("addons.gallifreyan_chronicles.empty", "No addons installed yet.");
        add("addons.gallifreyan_chronicles.open_folder", "Open Addons Folder");
        add("addons.gallifreyan_chronicles.installed", "Installed %s addon(s).");
        add("addons.gallifreyan_chronicles.install_none", "Nothing installed. Drop .zip or .jar files, or folders with an addon.json.");
        add("addons.gallifreyan_chronicles.restart", "Restart the game to apply changes to Java addons.");
        add("addons.gallifreyan_chronicles.restart_tag", "Restart needed");
        add("addons.gallifreyan_chronicles.page", "%s / %s");
        add("addons.gallifreyan_chronicles.by", "by %s");
        add("addons.gallifreyan_chronicles.type.data", "Data");
        add("addons.gallifreyan_chronicles.type.java", "Java");
        add("creativetab.gallifreyan_chronicles.addons", "GC: Addons");
        add("addons.gallifreyan_chronicles.problem.invalid", "Cannot be read: %s");
        add("addons.gallifreyan_chronicles.problem.incompatible_api", "Needs a newer addon API (%s, this mod has %s)");
        add("addons.gallifreyan_chronicles.problem.incompatible_mod", "Works with mod version %s, you have %s");
        add("addons.gallifreyan_chronicles.problem.missing_requirement", "Needs %s (%s)");
        add("addons.gallifreyan_chronicles.problem.wrong_requirement_version", "Needs %s %s, found %s");
        add("addons.gallifreyan_chronicles.problem.failed_to_start", "Failed to start: %s");
        add("addons.gallifreyan_chronicles.hint.invalid", "Check the addon.json inside the file.");
        add("addons.gallifreyan_chronicles.hint.incompatible_api", "Update Gallifreyan Chronicles, or use an older version of this addon.");
        add("addons.gallifreyan_chronicles.hint.incompatible_mod", "Get a version of this addon made for your version of the mod.");
        add("addons.gallifreyan_chronicles.hint.missing_requirement", "Install it and switch it on, then it will load.");
        add("addons.gallifreyan_chronicles.hint.wrong_requirement_version", "Update or replace what it needs.");
        add("addons.gallifreyan_chronicles.hint.failed_to_start", "The game log has the full error.");
        add("addons.gallifreyan_chronicles.state.active", "Active");
        add("addons.gallifreyan_chronicles.state.off", "Switched off");
        add("addons.gallifreyan_chronicles.state.restart_on", "Starts after a restart");
        add("addons.gallifreyan_chronicles.state.restart_off", "Stops after a restart");
        add("addons.gallifreyan_chronicles.details.select", "Select an addon to see its details.");
        add("addons.gallifreyan_chronicles.details.no_description", "No description.");
        add("addons.gallifreyan_chronicles.details.id", "ID: %s");
        add("addons.gallifreyan_chronicles.details.requires", "Needs %s %s");
        add("addons.gallifreyan_chronicles.details.file", "File: %s");
        add("options.gallifreyan_chronicles.header", "Gallifreyan Chronicles");
        add("options.gallifreyan_chronicles.immersive_portals", "Immersive Portals");
        add("options.gallifreyan_chronicles.immersive_portals.tooltip", "Use Immersive Portals for TARDIS doors. When off, doors use the plain teleport instead. Applies to worlds you host.");
        add("options.gallifreyan_chronicles.model_render_distance", "Model Render Distance");
        add("options.gallifreyan_chronicles.model_render_distance.tooltip", "How far away TARDIS models are rendered, in blocks.");
        add("options.gallifreyan_chronicles.model_render_distance.value", "%s blocks");
        add("options.gallifreyan_chronicles.portal_render_distance", "Portal Render Distance");
        add("options.gallifreyan_chronicles.portal_render_distance.tooltip", "How far away TARDIS door portals are rendered, in blocks.");
        add("options.gallifreyan_chronicles.max_rendered_portals", "Max Rendered Portals");
        add("options.gallifreyan_chronicles.max_rendered_portals.tooltip", "The most TARDIS door portals that can be rendered at once.");
        add("command.gallifreyan_chronicles.deletion_mode_on", "TARDIS deletion mode enabled. Breaking a TARDIS will now delete it.");
        add("command.gallifreyan_chronicles.deletion_mode_off", "TARDIS deletion mode disabled.");
        add("message.gallifreyan_chronicles.deletion_mode_required", "%s: enable deletion mode with %s to break this TARDIS.");
        add("options.gallifreyan_chronicles.power_darkness", "TARDIS Power-Off Darkness");
        add("options.gallifreyan_chronicles.power_darkness.tooltip", "How dark it gets inside a TARDIS when its power is turned off.");
        add("options.gallifreyan_chronicles.star_density", "Star Density");
        add("options.gallifreyan_chronicles.star_density.off", "Off");
        add("options.gallifreyan_chronicles.star_density.low", "Low");
        add("options.gallifreyan_chronicles.star_density.normal", "Normal");
        add("options.gallifreyan_chronicles.star_density.high", "High");
        add("options.gallifreyan_chronicles.star_density.tooltip", "How many stars appear in the skies of other planets.");
        add("message.gallifreyan_chronicles.key_linked", "The key is now linked to this TARDIS.");
        add("message.gallifreyan_chronicles.close_doors_to_lock", "Close both doors before locking!");
        add("message.gallifreyan_chronicles.no_destination_to_set", "This console has no TARDIS destination to set!");
        add("message.gallifreyan_chronicles.doors_locked", "The doors are locked!");
        add("message.gallifreyan_chronicles.doors_sealed", "The doors are sealed while the TARDIS is travelling!");
        add("message.gallifreyan_chronicles.lock_no_exterior", "The door lock has no linked TARDIS exterior!");
        add("message.gallifreyan_chronicles.isomorphic_denied", "Isomorphic security: only the owner can use these controls.");

        // Facing
        add("facing.gallifreyan_chronicles.north", "North");
        add("facing.gallifreyan_chronicles.north_east", "North-East");
        add("facing.gallifreyan_chronicles.east", "East");
        add("facing.gallifreyan_chronicles.south_east", "South-East");
        add("facing.gallifreyan_chronicles.south", "South");
        add("facing.gallifreyan_chronicles.south_west", "South-West");
        add("facing.gallifreyan_chronicles.west", "West");
        add("facing.gallifreyan_chronicles.north_west", "North-West");

        // Debug
        add("debug.gallifreyan_chronicles.control_position", "[%s] pos=(%s) offset=(%s) size=(%s)");
        add("debug.gallifreyan_chronicles.control_unset", "<unset>");

        add("tooltip.gallifreyan_chronicles.label_separator", ": ");

        // Config screen
        add("menu.gallifreyan_chronicles.config", "Gallifreyan Chronicles Config");
        add("menu.gallifreyan_chronicles.config.exterior", "Default Exterior");
        add("menu.gallifreyan_chronicles.config.console", "Default Console");
        add("menu.gallifreyan_chronicles.config.interior", "Default Interior");
        add("menu.gallifreyan_chronicles.config.hint", "Changes apply to newly created TARDISes");

        // Sound subtitles
        add("sounds.gallifreyan_chronicles.block_entity.tardis_door_open", "TARDIS door opens");
        add("sounds.gallifreyan_chronicles.block_entity.slider_sound", "TARDIS slider moves");
        add("sounds.gallifreyan_chronicles.block_entity.door_lock", "TARDIS door locks");
        add("sounds.gallifreyan_chronicles.block_entity.door_unlock", "TARDIS door unlocks");
        add("sounds.gallifreyan_chronicles.block_entity.is_locked", "TARDIS door is locked");
        add("sounds.gallifreyan_chronicles.block_entity.key_jammed", "TARDIS key jams");
        add("sounds.gallifreyan_chronicles.block_entity.first_doctors_hum", "TARDIS hums");
        add("gallifreyan_chronicles.title.tile_screen", "Gallifreyan Chronicles Main Theme");

        // Jade
        add("tooltip.gallifreyan_chronicles.tardis.owner", "Owner");
        add("tooltip.gallifreyan_chronicles.key.style", "Style");
        add("key_style.gallifreyan_chronicles.tardis", "TARDIS");
        add("key_style.gallifreyan_chronicles.gallifreyan", "Gallifreyan");
        add("key_style.gallifreyan_chronicles.yale", "Yale");
        add("tooltip.gallifreyan_chronicles.tardis.owner_unknown", "Unknown");
        add("config.jade.plugin_gallifreyan_chronicles.tardis_owner", "TARDIS Owner");
        add("config.jade.plugin_gallifreyan_chronicles.tardis_exterior_info", "TARDIS Exterior");
        add("config.jade.plugin_gallifreyan_chronicles.tardis_console_info", "TARDIS Console Model and Variant");
        add("tooltip.gallifreyan_chronicles.tardis.exterior", "Exterior");
        add("config.jade.plugin_gallifreyan_chronicles", "Gallifreyan Chronicles");
        add("config.jade.plugin_gallifreyan_chronicles.tardis_control", "TARDIS Control Icon");
        add("config.jade.plugin_gallifreyan_chronicles.tardis_block_icon", "TARDIS Exterior and Console Icon");
    }
}