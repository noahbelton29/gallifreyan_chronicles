package uk.nbforge.gallifreyan_chronicles;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.item.v1.BlockTransformerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.HudRenderer;
import uk.nbforge.gallifreyan_chronicles.client.input.GCKeyMappings;
import uk.nbforge.gallifreyan_chronicles.client.lifecycle.ClientState;
import uk.nbforge.gallifreyan_chronicles.client.protection.watermark.WatermarkIdentity;
import uk.nbforge.gallifreyan_chronicles.client.protection.watermark.WatermarkOverlay;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.InteriorDoorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisConsoleBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisExteriorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.entity.TardisControlEntityRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.entity.TardisKeyEntityRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.util.OutlineHider;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackOptionsButton;
import uk.nbforge.gallifreyan_chronicles.compat.replaymod.ReplayModOptionsButton;
import uk.nbforge.gallifreyan_chronicles.core.GCBlockEntities;
import uk.nbforge.gallifreyan_chronicles.core.GCEntityTypes;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.CadonwoodBlocks;
import uk.nbforge.gallifreyan_chronicles.network.client.GCFabricClientNetworking;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class GallifreyanChroniclesFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(ClientState::clearAll));
        WatermarkIdentity.enabled();

        BlockTransformerHelper.registerStripping(CadonwoodBlocks.CADONWOOD_LOG, CadonwoodBlocks.STRIPPED_CADONWOOD_LOG);
        BlockEntityRenderers.register(
                GCBlockEntities.TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE, TardisExteriorBlockRenderer::new);
        BlockEntityRenderers.register(
                GCBlockEntities.TARDIS_CONSOLE_BLOCK_ENTITY_TYPE, TardisConsoleBlockRenderer::new);
        BlockEntityRenderers.register(GCBlockEntities.INTERIOR_DOOR_BLOCK_ENTITY_TYPE, InteriorDoorBlockRenderer::new);
        EntityRenderers.register(GCEntityTypes.TARDIS_CONTROL_ENTITY_TYPE, TardisControlEntityRenderer::new);
        EntityRenderers.register(GCEntityTypes.TARDIS_KEY_ENTITY_TYPE, TardisKeyEntityRenderer::new);
        GCFabricClientNetworking.registerClientNetworking();

        KeyMapping.Category keyCategory = KeyMapping.Category.register(GCUtil.modIdentifier("controls"));
        KeyMappingHelper.registerKeyMapping(GCKeyMappings.createShowControlNames(keyCategory));

        if (FabricLoader.getInstance().isModLoaded("replaymod")) {
            ReplayModOptionsButton.register();
        }
        if (FabricLoader.getInstance().isModLoaded("flashback")) {
            FlashbackOptionsButton.register();
        }

        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((_, hit) -> !OutlineHider.shouldHide(hit.pos()));

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                GCUtil.modIdentifier("power_darkness"),
                (graphics, _) -> HudRenderer.renderBelowHud(graphics));

        HudElementRegistry.addLast(GCUtil.modIdentifier("hud_overlay"), (graphics, _) -> HudRenderer.render(graphics));

        HudElementRegistry.addLast(
                GCUtil.modIdentifier("watermark_overlay"), (graphics, _) -> WatermarkOverlay.render(graphics));
    }
}
