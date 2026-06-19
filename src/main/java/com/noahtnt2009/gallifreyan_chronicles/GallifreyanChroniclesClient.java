package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.client.overlay.HudOverlay;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlockEntities;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import java.util.List;

public class GallifreyanChroniclesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
//        HudOverlay.init();
        BlockColorRegistry.register(
                List.of(BlockTintSources.constant(0xFFE0E0E0, 0xFFE0E0E0)),
                GCBlocks.SHORT_GALLIFREYAN_GRASS,
                GCBlocks.TALL_GALLIFREYAN_GRASS
        );

        StrippableBlockRegistry.register(GCBlocks.CADONWOOD_LOG, GCBlocks.STRIPPED_CADONWOOD_LOG);
        BlockEntityRenderers.register(GCBlockEntities.TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE, TardisExteriorBlockRenderer::new);
    }
}