package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.ecs.component.ComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.registry.*;
import com.noahtnt2009.gallifreyan_chronicles.world.biome.GCSurfaceRules;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import terrablender.api.SurfaceRuleManager;
import terrablender.api.TerraBlenderApi;

public class GallifreyanChronicles implements ModInitializer, TerraBlenderApi {
	public static final String MOD_ID = "gallifreyan_chronicles";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ComponentTypes.init();
		GCItems.init();
		GCBlocks.init();
		GCBlockEntities.init();
		GCExteriors.init();
		GCCreativeModeTabs.init();
		GCWorldGeneration.init();
		GCCommands.init();
		GCSounds.init();
	}

	@Override
	public void onTerraBlenderInitialized() {
		GCBiomes.registerBiomes();

		SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MOD_ID, GCSurfaceRules.gallifrey());
	}
}