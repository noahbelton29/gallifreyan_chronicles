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
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GCModpackDataProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.NOISE_SETTINGS, GCDimensions::bootstrapNoise)
            .add(Registries.FEATURE, GCConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, GCPlacedFeatures::bootstrap)
            .add(Registries.DIMENSION_TYPE, GCDimensions::bootstrapType)
            .add(Registries.LEVEL_STEM, GCDimensions::bootstrapStem)
            .add(Registries.BIOME, GCBiomes::bootstrap);

    public static final RegistrySetBuilder RELOADABLE_BUILDER =
            new RegistrySetBuilder()
                    .add(
                            Registries.LOOT_TABLE,
                            new LootTableProvider(
                                    Set.of(),
                                    List.of(
                                            new LootTableProvider.SubProviderEntry(
                                                    GCBlockLootTableProvider::new,
                                                    LootContextParamSets.BLOCK
                                            )
                                    )
                            )
                    )
                    .add(GCRecipeProvider.bootstrap());

    public static DatapackBuiltinEntriesProvider create(
            net.minecraft.data.PackOutput output,
            java.util.concurrent.CompletableFuture<
                    net.minecraft.core.HolderLookup.Provider> registries) {

        return DatapackBuiltinEntriesProvider.forWorldLayer(
                output,
                "Gallifreyan Chronicles worldgen",
                registries,
                BUILDER,
                Set.of(Constants.MOD_ID)
        );
    }

    public static DatapackBuiltinEntriesProvider createReloadable(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> worldRegistries,
            CompletableFuture<HolderLookup.Provider> reloadableRegistries) {

        return DatapackBuiltinEntriesProvider.forReloadableLayer(
                output,
                "Gallifreyan Chronicles reloadable data",
                worldRegistries,
                reloadableRegistries,
                RELOADABLE_BUILDER,
                Set.of(Constants.MOD_ID)
        );
    }
}