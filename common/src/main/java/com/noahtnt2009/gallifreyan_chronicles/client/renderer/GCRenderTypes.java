package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class GCRenderTypes {
    private static final Map<Identifier, RenderType> LAYERED_EYES = new HashMap<>();

    private GCRenderTypes() {
    }

    public static RenderType layeredEyes(Identifier texture) {
        return LAYERED_EYES.computeIfAbsent(texture, t -> RenderType.create(
                "gc_layered_eyes",
                RenderSetup.builder(RenderPipelines.EYES)
                        .withTexture("Sampler0", t)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .sortOnUpload()
                        .createRenderSetup()));
    }
}