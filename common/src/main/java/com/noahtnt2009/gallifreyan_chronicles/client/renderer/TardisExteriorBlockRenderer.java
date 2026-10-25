package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisExteriorBlockModel;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorGlowLayer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorKeyLayer;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TardisExteriorBlockRenderer extends GeoBlockRenderer<@NotNull TardisExteriorBlockEntity, @NotNull TardisExteriorBlockRenderState> {
    public static final DataTicket<String> EXTERIOR_ID = DataTicket.create("exterior_id", String.class);
    public static final DataTicket<Float>  YAW = DataTicket.create("tardis_yaw", Float.class);
    public static final DataTicket<Boolean> GLOWING = DataTicket.create("tardis_glowing", Boolean.class);
    public static final DataTicket<Float> OPACITY = DataTicket.create("tardis_opacity", Float.class);

    private static final float GLASS_STRENGTH = .1f;
    private static final float GLASS_SPEED = 1.0f;
    private static final int DEPTH_MASK_ORDER = -1;
    private static final float DEPTH_MASK_PUSH = 1.0002f;

    public TardisExteriorBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new TardisExteriorBlockModel());

        withRenderLayer(TardisExteriorGlowLayer::new);
        withRenderLayer(renderer -> new TardisExteriorKeyLayer<>(renderer, context));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NonNull TardisExteriorBlockRenderState createRenderState() {
        return new TardisExteriorBlockRenderState();
    }

    @Override
    public void addRenderData(TardisExteriorBlockEntity animatable, Void relatedObject,
                              TardisExteriorBlockRenderState renderState, float partialTick) {
        renderState.addGeckolibData(EXTERIOR_ID, animatable.getExterior().id());
        renderState.addGeckolibData(YAW, animatable.getYaw());
        renderState.addGeckolibData(GLOWING, animatable.isGlowing());
        renderState.addGeckolibData(OPACITY, animatable.getOpacity(partialTick));
    }

    @Override
    public int getRenderColor(TardisExteriorBlockEntity animatable, @Nullable Void relatedObject, float partialTick) {
        float opacity = Math.clamp(animatable.getOpacity(partialTick), 0.0f, 1.0f);
        float faded = 1.0f - opacity;
        int alpha = Math.round(opacity * 255.0f);
        int red = Math.round((1.0f - 0.25f * faded) * 255.0f);
        int green = Math.round((1.0f - 0.08f * faded) * 255.0f);
        return (alpha << 24) | (red << 16) | (green << 8) | 0xFF;
    }

    @Override
    public int getPackedOverlay(TardisExteriorBlockEntity animatable, @Nullable Void relatedObject, float u, float partialTick) {
        float faded = 1.0f - Math.clamp(animatable.getOpacity(partialTick), 0.0f, 1.0f);
        float t = Math.clamp(faded * GLASS_SPEED, 0.0f, 1.0f);
        float glass = t * t * (3.0f - 2.0f * t) * GLASS_STRENGTH;
        return OverlayTexture.pack(OverlayTexture.u(glass), OverlayTexture.v(false));
    }

    @Override
    public @Nullable RenderType getRenderType(TardisExteriorBlockRenderState renderState, Identifier texture) {
        float opacity = renderState.getOrDefaultGeckolibData(OPACITY, 1.0f);
        if (opacity <= 0.0f) return null;
        if (opacity < 1.0f) return RenderTypes.entityTranslucentCull(texture);
        return super.getRenderType(renderState, texture);
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo,
                                  OrderedSubmitNodeCollector renderTasks, @Nullable RenderType renderType) {
        float opacity = renderPassInfo.renderState().getOrDefaultGeckolibData(OPACITY, 1.0f);
        if (renderType != null && opacity < 1.0f) {
            OrderedSubmitNodeCollector maskTasks = renderTasks instanceof SubmitNodeCollector collector
                    ? collector.order(DEPTH_MASK_ORDER)
                    : renderTasks;
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.pushPose();
            poseStack.last().pose().scaleLocal(DEPTH_MASK_PUSH, DEPTH_MASK_PUSH, DEPTH_MASK_PUSH);
            super.submitRenderTasks(renderPassInfo, maskTasks, RenderTypes.waterMask());
            poseStack.popPose();
        }
        super.submitRenderTasks(renderPassInfo, renderTasks, renderType);
    }

    @Override
    public void adjustModelBonesForRender(@NonNull RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);

        float opacity = renderPassInfo.renderState().getOrDefaultGeckolibData(OPACITY, 1.0f);
        if (opacity < 1.0f) {
            snapshots.ifPresent(TardisExteriorKeyLayer.KEY_BONE_NAME, snapshot -> snapshot.skipRender(true));
        }
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        Float yaw = renderPassInfo.renderState().getGeckolibData(YAW);
        renderPassInfo.poseStack().rotateDegrees(
                Axis.YP,
                TardisExterior.modelRotationDegrees(yaw != null ? yaw : 0f)
        );
    }
}