package uk.nbforge.gallifreyan_chronicles.client.renderer.block;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.client.model.InteriorDoorBlockModel;
import uk.nbforge.gallifreyan_chronicles.client.renderer.util.RenderLevelGuard;
import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import uk.nbforge.gallifreyan_chronicles.client.util.FadeTintUtil;
import uk.nbforge.gallifreyan_chronicles.client.util.ModelRenderRangeUtil;
import uk.nbforge.gallifreyan_chronicles.compat.portal.PortalBone;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.TardisExterior;

public class InteriorDoorBlockRenderer
        extends GeoBlockRenderer<@NonNull InteriorDoorBlockEntity, @NonNull TardisExteriorBlockRenderState> {
    public static final DataTicket<String> VARIANT = DataTicket.create("interior_door_variant", String.class);
    public static final DataTicket<String> TEXTURE_ID = DataTicket.create("interior_door_texture_id", String.class);
    public static final DataTicket<String> ROUNDEL = DataTicket.create("interior_door_roundel", String.class);
    public static final DataTicket<Boolean> FLIPPED = DataTicket.create("interior_door_flipped", Boolean.class);
    public static final DataTicket<Boolean> FLUSH = DataTicket.create("interior_door_flush", Boolean.class);
    public static final DataTicket<Float> YAW = DataTicket.create("interior_door_yaw", Float.class);
    public static final DataTicket<Float> REMAT = DataTicket.create("interior_door_remat_opacity", Float.class);

    private static final java.util.Set<Identifier> LOGGED_SKINS = new java.util.HashSet<>();

    public InteriorDoorBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new InteriorDoorBlockModel());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NonNull TardisExteriorBlockRenderState createRenderState() {
        return new TardisExteriorBlockRenderState();
    }

    @Override
    public void addRenderData(
            InteriorDoorBlockEntity animatable,
            Void relatedObject,
            TardisExteriorBlockRenderState renderState,
            float partialTick) {
        RenderLevelGuard.tag(renderState, animatable.getLevel());
        renderState.addGeckolibData(VARIANT, animatable.getVariant());
        renderState.addGeckolibData(TEXTURE_ID, animatable.getTextureId());
        renderState.addGeckolibData(ROUNDEL, animatable.getRoundelBlock());
        renderState.addGeckolibData(YAW, animatable.getFacing().toYRot());
        renderState.addGeckolibData(FLIPPED, animatable.isFlipped());
        renderState.addGeckolibData(FLUSH, animatable.isFlush());
        renderState.addGeckolibData(REMAT, RematCatchClient.opacityIn(animatable.getLevel()));
    }

    @Override
    public void adjustRenderPose(@NonNull RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        Float yawValue = renderPassInfo.renderState().getGeckolibData(YAW);
        float yaw = yawValue != null ? yawValue : 0f;
        boolean flipped = Boolean.TRUE.equals(renderPassInfo.renderState().getGeckolibData(FLIPPED));
        boolean flush = Boolean.TRUE.equals(renderPassInfo.renderState().getGeckolibData(FLUSH));
        Direction facing = Direction.fromYRot(yaw);
        Direction right = InteriorDoorBlock.rightDirectionOf(facing, flipped);

        if (flush) {
            renderPassInfo
                    .poseStack()
                    .translate(
                            right.getStepX() * 0.5f - facing.getStepX() * 0.5f,
                            0.75f,
                            right.getStepZ() * 0.5f - facing.getStepZ() * 0.5f);
            renderPassInfo.poseStack().rotateDegrees(Axis.YP, TardisExterior.modelRotationDegrees(yaw));
            renderPassInfo.poseStack().rotateDegrees(Axis.XP, -90f);
            return;
        }

        renderPassInfo.poseStack().translate(right.getStepX() * 0.5f, 0f, right.getStepZ() * 0.5f);
        renderPassInfo.poseStack().rotateDegrees(Axis.YP, TardisExterior.modelRotationDegrees(yaw));
        if (flipped) {
            renderPassInfo.poseStack().translate(0f, -0.5f, 0f);
            renderPassInfo.poseStack().rotateDegrees(Axis.ZP, 180f);
            renderPassInfo.poseStack().translate(0f, -1.5f, 0f);
        }
    }

    @Override
    public void submit(
            @NonNull TardisExteriorBlockRenderState renderState,
            @NonNull PoseStack poseStack,
            @NonNull SubmitNodeCollector submitNodeCollector,
            @NonNull CameraRenderState cameraState) {
        if (RenderLevelGuard.belongsToOtherLevel(renderState)) return;
        super.submit(renderState, poseStack, submitNodeCollector, cameraState);
    }

    @Override
    public boolean shouldRender(@NonNull InteriorDoorBlockEntity blockEntity, @NonNull Vec3 cameraPos) {
        return ModelRenderRangeUtil.inRangeAndLocal(blockEntity, cameraPos);
    }

    @Override
    public int getViewDistance() {
        return GCConfig.modelRenderDistance();
    }

    @Override
    public void adjustModelBonesForRender(
            @NonNull RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo, @NonNull BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        snapshots.ifPresent(PortalBone.BONE_NAME, snapshot -> snapshot.skipRender(true));
    }

    @Override
    public int getRenderColor(InteriorDoorBlockEntity animatable, @Nullable Void relatedObject, float partialTick) {
        int color = super.getRenderColor(animatable, relatedObject, partialTick);
        float opacity = RematCatchClient.opacityIn(animatable.getLevel());
        return opacity < 1.0f ? ARGB.multiply(color, FadeTintUtil.of(opacity)) : color;
    }

    @Override
    public @Nullable RenderType getRenderType(TardisExteriorBlockRenderState renderState, Identifier texture) {
        float opacity = renderState.getOrDefaultGeckolibData(REMAT, 1.0f);
        if (opacity <= 0.0f) return null;
        Identifier skin = InteriorDoorBlockModel.textureFor(renderState);
        if (!skin.equals(texture) || skin.getPath().startsWith("dynamic/")) {
            if (LOGGED_SKINS.add(skin)) {
                uk.nbforge.gallifreyan_chronicles.Constants.LOG.info(
                        "Interior door is drawn with {} (GeckoLib offered {})", skin, texture);
            }
        }
        if (opacity < 1.0f) return RenderTypes.entityTranslucentCull(skin);
        return super.getRenderType(renderState, skin);
    }

    @Override
    public void submitRenderTasks(
            RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo,
            OrderedSubmitNodeCollector renderTasks,
            @Nullable RenderType renderType) {
        float opacity = renderPassInfo.renderState().getOrDefaultGeckolibData(REMAT, 1.0f);
        if (renderType != null && opacity < 1.0f) {
            OrderedSubmitNodeCollector maskTasks = renderTasks instanceof SubmitNodeCollector collector
                    ? collector.order(RematCatchClient.MASK_ORDER)
                    : renderTasks;
            PoseStack poseStack = renderPassInfo.poseStack();
            poseStack.pushPose();
            poseStack
                    .last()
                    .pose()
                    .scaleLocal(RematCatchClient.MASK_PUSH, RematCatchClient.MASK_PUSH, RematCatchClient.MASK_PUSH);
            super.submitRenderTasks(renderPassInfo, maskTasks, RenderTypes.waterMask());
            poseStack.popPose();
        }
        super.submitRenderTasks(renderPassInfo, renderTasks, renderType);
    }
}
