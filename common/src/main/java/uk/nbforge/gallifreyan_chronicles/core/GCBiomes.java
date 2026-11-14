package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import uk.nbforge.gallifreyan_chronicles.core.world.biomes.GCGallifreyBiomes;
import uk.nbforge.gallifreyan_chronicles.core.world.biomes.GCMoonBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class GCBiomes {
    public static final ResourceKey<Biome> GALLIFREYAN_DESERT = registerBiomeKey("gallifreyan_desert");
    public static final ResourceKey<Biome> GALLIFREYAN_PLAINS = registerBiomeKey("gallifreyan_plains");
    public static final ResourceKey<Biome> GALLIFREYAN_BADLANDS = registerBiomeKey("gallifreyan_badlands");

    public static final ResourceKey<Biome> MOON = registerBiomeKey("moon");

    public static void bootstrap(BootstrapContext<Biome> context) {
        var carvers = context.lookup(Registries.CARVER);
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        register(context, GALLIFREYAN_DESERT, GCGallifreyBiomes.gallifreyanDesert(placedFeatures, carvers));
        register(context, GALLIFREYAN_PLAINS, GCGallifreyBiomes.gallifreyanPlains(placedFeatures, carvers));
        register(context, GALLIFREYAN_BADLANDS, GCGallifreyBiomes.gallifreyanBadlands(placedFeatures, carvers));
        register(context, MOON, GCMoonBiomes.moon(placedFeatures, carvers));
    }

    private static void register(BootstrapContext<Biome> context, ResourceKey<Biome> key, Biome biome) {
        context.register(key, biome);
    }

    private static ResourceKey<Biome> registerBiomeKey(String name) {
        return ResourceKey.create(Registries.BIOME, GCUtil.modIdentifier(name));
    }
}
