package uk.nbforge.gallifreyan_chronicles.client.renderer.block;

import uk.nbforge.gallifreyan_chronicles.client.renderer.fade.FadeBoxRenderer;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import uk.nbforge.gallifreyan_chronicles.client.renderer.geo_layer.TardisLampOrbLayer;
import uk.nbforge.gallifreyan_chronicles.client.util.FadeTintUtil;

import uk.nbforge.gallifreyan_chronicles.client.util.ModelRenderRangeUtil;

import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import com.geckolib.renderer.base.BoneSnapshots;
import uk.nbforge.gallifreyan_chronicles.compat.portal.PortalBone;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import uk.nbforge.gallifreyan_chronicles.client.model.TardisExteriorBlockModel;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorGlowLayer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorKeyLayer;
import uk.nbforge.gallifreyan_chronicles.client.sky.SkyShaderCompat;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;
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
import uk.nbforge.gallifreyan_chronicles.client.renderer.util.RenderLevelGuard;

public class TardisExteriorBlockRenderer extends GeoBlockRenderer<@NotNull TardisExteriorBlockEntity, @NotNull TardisExteriorBlockRenderState> {
    public static final DataTicket<String> EXTERIOR_ID = DataTicket.create("exterior_id", String.class);
    public static final DataTicket<Float> YAW = DataTicket.create("tardis_yaw", Float.class);
    public static final DataTicket<Boolean> GLOWING = DataTicket.create("tardis_glowing", Boolean.class);
    /* how bright the lamp is, 0 to 1, for the takeoff and landing flicker */
    public static final DataTicket<Float> LAMP = DataTicket.create("tardis_lamp", Float.class);
    public static final DataTicket<Float> LAMP_ORB = DataTicket.create("tardis_lamp_orb", Float.class);
    /* which way the camera is looking, saved each frame for the lamp orb */
    public static final Quaternionf cameraOrientation = new Quaternionf();
    /* where this block's corner is relative to the camera, saved each frame for the lamp orb */
    public static final Vector3f blockOrigin = new Vector3f();
    public static final DataTicket<Float> OPACITY = DataTicket.create("tardis_opacity", Float.class);

    private static final float GLASS_STRENGTH = .1f;
    private static final float GLASS_SPEED = 1.0f;
    private static final int DEPTH_MASK_ORDER = -1;
    private static final float DEPTH_MASK_PUSH = 1.00005f;

    public TardisExteriorBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new TardisExteriorBlockModel());

        withRenderLayer(TardisExteriorGlowLayer::new);
        withRenderLayer(renderer -> new TardisExteriorKeyLayer<>(renderer, context));
        withRenderLayer(TardisLampOrbLayer::new);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NonNull TardisExteriorBlockRenderState createRenderState() {
        return new TardisExteriorBlockRenderState();
    }

    @Override
    public void addRenderData(TardisExteriorBlockEntity animatable, Void relatedObject,
                              TardisExteriorBlockRenderState renderState, float partialTick) {
        RenderLevelGuard.tag(renderState, animatable.getLevel());
        renderState.addGeckolibData(EXTERIOR_ID, animatable.getExterior().id());
        renderState.addGeckolibData(YAW, animatable.getYaw());
        boolean hiddenFromInterior = isSeenFromItsInterior(animatable);
        renderState.addGeckolibData(GLOWING, animatable.isGlowing() && !hiddenFromInterior);
        renderState.addGeckolibData(LAMP, animatable.lampBrightness(partialTick));
        renderState.addGeckolibData(LAMP_ORB, hiddenFromInterior ? 0f : animatable.lampOrb(partialTick));
        renderState.addGeckolibData(OPACITY, hiddenFromInterior ? 0.0f : animatable.getOpacity(partialTick));
    }

    private static boolean isSeenFromItsInterior(TardisExteriorBlockEntity animatable) {
        if (!animatable.isPortalActive() || !SkyShaderCompat.isShaderPackInUse()) return false;
        return TardisPortalBridge.isViewingThroughPortalNear(Vec3.atBottomCenterOf(animatable.getBlockPos()), 2.0);
    }

    @Override
    public int getRenderColor(TardisExteriorBlockEntity animatable, @Nullable Void relatedObject, float partialTick) {
        float opacity = Math.clamp(animatable.getOpacity(partialTick), 0.0f, 1.0f);
        return FadeTintUtil.of(opacity);
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
        if (opacity < 1.0f) {
            return switch (FadeBoxRenderer.mode()) {
                case CANVAS -> FadeBoxRenderer.renderType(texture);
                case MASK -> RenderTypes.entityTranslucentCull(texture);
            };
        }
        return super.getRenderType(renderState, texture);
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo,
                                  OrderedSubmitNodeCollector renderTasks, @Nullable RenderType renderType) {
        float opacity = renderPassInfo.renderState().getOrDefaultGeckolibData(OPACITY, 1.0f);
        /* the depth mask is only needed for the old way (shaders, portals) */
        if (renderType != null && opacity < 1.0f && FadeBoxRenderer.mode() == FadeBoxRenderer.Mode.MASK) {
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
        /* portal bone is only a marker */
        snapshots.ifPresent(PortalBone.BONE_NAME, snapshot -> snapshot.skipRender(true));

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

    @Override
    public void submit(@NonNull TardisExteriorBlockRenderState renderState, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState cameraState) {
        if (RenderLevelGuard.belongsToOtherLevel(renderState)) return;
        /* the lamp orb needs these to face the camera. skip iris's shadow pass, that's the sun's view not yours */
        if (!SkyShaderCompat.isRenderingShadowPass()) {
            cameraOrientation.set(cameraState.orientation);
            poseStack.last().pose().transformPosition(blockOrigin.set(0f, 0f, 0f));
        }
        super.submit(renderState, poseStack, submitNodeCollector, cameraState);
        /* geckolib skips a fully see-through box, so the orb draws itself then */
        if (renderState.getOrDefaultGeckolibData(OPACITY, 1.0f) <= 0.0f) {
            TardisLampOrbLayer.drawWithoutBox(renderState, poseStack, submitNodeCollector);
        }
    }

    @Override
    public boolean shouldRender(@NonNull TardisExteriorBlockEntity blockEntity, @NonNull Vec3 cameraPos) {
        return ModelRenderRangeUtil.inRangeAndLocal(blockEntity, cameraPos);
    }

    @Override
    public int getViewDistance() {
        return GCConfig.modelRenderDistance();
    }
}