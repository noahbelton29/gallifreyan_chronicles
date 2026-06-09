package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.datagen.*;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.world.GCConfiguredFeatures;
import com.noahtnt2009.gallifreyan_chronicles.world.GCPlacedFeatures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class GallifreyanChroniclesDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(GCRegistryDataProvider::new);
		pack.addProvider(GCLanguageProvider::new);
		pack.addProvider(GCModelProvider::new);
		pack.addProvider(GCBlockTagProvider::new);
		pack.addProvider(GCBlockLootTableProvider::new);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(Registries.NOISE_SETTINGS, GCDimensions::bootstrapNoise);
		registryBuilder.add(Registries.DIMENSION_TYPE, GCDimensions::bootstrapType);
		registryBuilder.add(Registries.LEVEL_STEM, GCDimensions::bootstrapStem);
		registryBuilder.add(Registries.CONFIGURED_FEATURE, GCConfiguredFeatures::bootstrap);
		registryBuilder.add(Registries.PLACED_FEATURE, GCPlacedFeatures::bootstrap);
		registryBuilder.add(Registries.BIOME, GCBiomes::bootstrap);
	}
}
