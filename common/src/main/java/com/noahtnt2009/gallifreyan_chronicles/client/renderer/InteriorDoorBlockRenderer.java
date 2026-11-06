package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.client.model.InteriorDoorBlockModel;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class InteriorDoorBlockRenderer extends GeoBlockRenderer<@NotNull InteriorDoorBlockEntity, @NotNull TardisExteriorBlockRenderState> {
    public static final DataTicket<String> VARIANT = DataTicket.create("interior_door_variant", String.class);
    public static final DataTicket<String> TEXTURE_ID = DataTicket.create("interior_door_texture_id", String.class);
    public static final DataTicket<Boolean> FLIPPED = DataTicket.create("interior_door_flipped", Boolean.class);
    public static final DataTicket<Boolean> FLUSH = DataTicket.create("interior_door_flush", Boolean.class);
    public static final DataTicket<Float> YAW = DataTicket.create("interior_door_yaw", Float.class);

    public InteriorDoorBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new InteriorDoorBlockModel());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NonNull TardisExteriorBlockRenderState createRenderState() {
        return new TardisExteriorBlockRenderState();
    }

    @Override
    public void addRenderData(InteriorDoorBlockEntity animatable, Void relatedObject,
                              TardisExteriorBlockRenderState renderState, float partialTick) {
        RenderLevelGuard.tag(renderState, animatable.getLevel());
        renderState.addGeckolibData(VARIANT, animatable.getVariant());
        renderState.addGeckolibData(TEXTURE_ID, animatable.getTextureId());
        renderState.addGeckolibData(YAW, animatable.getFacing().toYRot());
        renderState.addGeckolibData(FLIPPED, animatable.isFlipped());
        renderState.addGeckolibData(FLUSH, animatable.isFlush());
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
            renderPassInfo.poseStack().translate(right.getStepX() * 0.5f - facing.getStepX() * 0.5f, 0.75f,
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
    public void submit(@NonNull TardisExteriorBlockRenderState renderState, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState cameraState) {
        if (RenderLevelGuard.belongsToOtherLevel(renderState)) return;
        super.submit(renderState, poseStack, submitNodeCollector, cameraState);
    }

    @Override
    public boolean shouldRender(@NonNull InteriorDoorBlockEntity blockEntity, @NonNull Vec3 cameraPos) {
        if (RenderLevelGuard.isForeign(blockEntity.getLevel())) return false;
        double range = GCConfig.modelRenderDistance();
        return Vec3.atCenterOf(blockEntity.getBlockPos()).distanceToSqr(cameraPos) < range * range;
    }

    @Override
    public int getViewDistance() {
        return GCConfig.modelRenderDistance();
    }
}