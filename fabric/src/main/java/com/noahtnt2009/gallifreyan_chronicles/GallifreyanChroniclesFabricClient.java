package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.client.hud.GCHudRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisConsoleBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisControlEntityRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisKeyEntityRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlockEntities;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCEntityTypes;
import com.noahtnt2009.gallifreyan_chronicles.network.client.GCFabricClientNetworking;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class GallifreyanChroniclesFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
//        BlockColorRegistry.register(
//                List.of(BlockTintSources.constant(0xFF590a0a, 0xFF590a0a)),
//                GCBlocks.SHORT_GALLIFREYAN_GRASS,
//                GCBlocks.TALL_GALLIFREYAN_GRASS
//        );


        StrippableBlockRegistry.register(GCBlocks.CADONWOOD_LOG, GCBlocks.STRIPPED_CADONWOOD_LOG);
        BlockEntityRenderers.register(GCBlockEntities.TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE, TardisExteriorBlockRenderer::new);
        BlockEntityRenderers.register(GCBlockEntities.TARDIS_CONSOLE_BLOCK_ENTITY_TYPE, TardisConsoleBlockRenderer::new);
        EntityRenderers.register(GCEntityTypes.TARDIS_CONTROL_ENTITY_TYPE, TardisControlEntityRenderer::new);
        EntityRenderers.register(GCEntityTypes.TARDIS_KEY_ENTITY_TYPE, TardisKeyEntityRenderer::new);
        GCFabricClientNetworking.registerClientNetworking();

        HudElementRegistry.addLast(
                GCUtils.of("hud_overlay"),
                (graphics, tickCounter) -> GCHudRenderer.render((GuiGraphicsExtractor) graphics)
        );

//        ImGuiMCEvents.INSTANCE.imGuiLoadPre(() -> {
//            ImGui.getIO().addConfigFlags(
//                    ImGuiConfigFlags.NavEnableKeyboard |
//                            ImGuiConfigFlags.NavEnableGamepad |
//                            ImGuiConfigFlags.NavEnableSetMousePos |
//                            ImGuiConfigFlags.DockingEnable |
//                            ImGuiConfigFlags.ViewportsEnable);
//        });
//        ImGuiMCEvents.INSTANCE.preRenderImGuiEvent(() -> {
//            TardisExteriorDebugInfo.Info info = TardisExteriorDebugInfo.getLookedAt();
//            Minecraft mc = Minecraft.getInstance();
//            if (mc.player == null || mc.level == null) return;
//
//            HitResult hit = mc.player.pick(20.0, 0.0f, false);
//            if (hit.getType() != HitResult.Type.BLOCK) return;
//
//            BlockHitResult blockHit = (BlockHitResult) hit;
//            if (!(mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof TardisExteriorBlockEntity tardisBE)) return;
//            if (info != null) {
//                ImGui.setNextWindowBgAlpha(0.6f);
//                if (ImGui.begin("TARDIS Info",
//                                ImGuiWindowFlags.NoFocusOnAppearing)) {
//                    ImGui.text("TARDIS ID: " + (tardisBE.getTardisId() != null ? tardisBE.getTardisId() : "unlinked"));
//                    ImGui.text("Current Exterior: " + tardisBE.getExterior().id());
//
//                    ImGui.text("Change Exterior:");
//                    ArrayList<TardisExterior> exteriors = new ArrayList<>(TardisExteriorRegistry.getAll());
//                    String[] exteriorNames = exteriors.stream().map(TardisExterior::id).toArray(String[]::new);
//                    ImInt currentExteriorIndex = new ImInt(Math.max(0, exteriors.indexOf(tardisBE.getExterior())));
//
//                    if (ImGui.combo("##exterior", currentExteriorIndex, exteriorNames)) {
//                        TardisExterior selected = exteriors.get(currentExteriorIndex.get());
//                        tardisBE.setExterior(selected);
//                    }
//
//                    ImGui.separator();
//                }
//                ImGui.end();
//            }
//        });
    }
}
