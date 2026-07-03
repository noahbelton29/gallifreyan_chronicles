package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlockEntities;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

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
