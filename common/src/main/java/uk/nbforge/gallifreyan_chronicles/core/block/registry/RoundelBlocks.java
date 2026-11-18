package uk.nbforge.gallifreyan_chronicles.core.block.registry;

import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.register;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public final class RoundelBlocks {
    private RoundelBlocks() {}

    public static final List<String> ROUNDEL_WOODS = List.of(
            "oak",
            "spruce",
            "birch",
            "jungle",
            "acacia",
            "dark_oak",
            "mangrove",
            "cherry",
            "pale_oak",
            "bamboo",
            "crimson",
            "warped",
            "cadonwood");
    public static final Map<String, Block> WOOD_ROUNDELS = new LinkedHashMap<>();
    public static final Map<String, Block> WOOD_HALF_ROUNDELS = new LinkedHashMap<>();

    public static final List<String> ROUNDEL_CONCRETES = List.of(
            "white",
            "light_gray",
            "gray",
            "black",
            "brown",
            "red",
            "orange",
            "yellow",
            "lime",
            "green",
            "cyan",
            "light_blue",
            "blue",
            "purple",
            "magenta",
            "pink");
    public static final Map<String, Block> CONCRETE_ROUNDELS = new LinkedHashMap<>();
    public static final Map<String, Block> CONCRETE_HALF_ROUNDELS = new LinkedHashMap<>();

    static {
        for (String wood : ROUNDEL_WOODS) {
            WOOD_ROUNDELS.put(wood, registerWoodRoundel(wood + "_roundel_block", wood));
            WOOD_HALF_ROUNDELS.put(wood, registerWoodRoundel(wood + "_half_roundel_block", wood));
        }
        for (String colour : ROUNDEL_CONCRETES) {
            CONCRETE_ROUNDELS.put(colour, registerConcreteRoundel(colour + "_concrete_roundel_block", colour));
            CONCRETE_HALF_ROUNDELS.put(
                    colour, registerConcreteRoundel(colour + "_concrete_half_roundel_block", colour));
        }
    }

    public static Block woodPlanks(String wood) {
        return switch (wood) {
            case "oak" -> Blocks.OAK_PLANKS;
            case "spruce" -> Blocks.SPRUCE_PLANKS;
            case "birch" -> Blocks.BIRCH_PLANKS;
            case "jungle" -> Blocks.JUNGLE_PLANKS;
            case "acacia" -> Blocks.ACACIA_PLANKS;
            case "dark_oak" -> Blocks.DARK_OAK_PLANKS;
            case "mangrove" -> Blocks.MANGROVE_PLANKS;
            case "cherry" -> Blocks.CHERRY_PLANKS;
            case "pale_oak" -> Blocks.PALE_OAK_PLANKS;
            case "bamboo" -> Blocks.BAMBOO_PLANKS;
            case "crimson" -> Blocks.CRIMSON_PLANKS;
            case "warped" -> Blocks.WARPED_PLANKS;
            case "cadonwood" -> CadonwoodBlocks.CADONWOOD_PLANKS;
            default -> throw new IllegalArgumentException("Unknown roundel wood " + wood);
        };
    }

    public static Block concrete(String colour) {
        return Blocks.CONCRETE.pick(DyeColor.byName(colour, DyeColor.WHITE));
    }

    private static Block registerWoodRoundel(String name, String wood) {
        Block planks = woodPlanks(wood);
        boolean nether = wood.equals("crimson") || wood.equals("warped");
        SoundType sound = switch (wood) {
            case "cherry" -> SoundType.CHERRY_WOOD;
            case "bamboo" -> SoundType.BAMBOO_WOOD;
            case "crimson", "warped" -> SoundType.NETHER_WOOD;
            default -> SoundType.WOOD;
        };
        return register(name, properties -> {
            properties
                    .mapColor(planks.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f, 3f)
                    .sound(sound);
            if (!nether) properties.ignitedByLava();
            return new Block(properties);
        });
    }

    private static Block registerConcreteRoundel(String name, String colour) {
        DyeColor dye = DyeColor.byName(colour, DyeColor.WHITE);
        return register(
                name,
                properties -> new Block(properties
                        .mapColor(dye)
                        .instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops()
                        .strength(1.8f)));
    }

    public static void init() {}
}
