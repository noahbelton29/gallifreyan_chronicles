package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.world.GCConfiguredFeatures;
import com.noahtnt2009.gallifreyan_chronicles.world.GCPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GCModpackDataProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.NOISE_SETTINGS, GCDimensions::bootstrapNoise)
            .add(Registries.CONFIGURED_FEATURE, GCConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, GCPlacedFeatures::bootstrap)
            .add(Registries.DIMENSION_TYPE, GCDimensions::bootstrapType)
            .add(Registries.LEVEL_STEM, GCDimensions::bootstrapStem)
            .add(Registries.BIOME, GCBiomes::bootstrap);

    public GCModpackDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Constants.MOD_ID));
    }
}
