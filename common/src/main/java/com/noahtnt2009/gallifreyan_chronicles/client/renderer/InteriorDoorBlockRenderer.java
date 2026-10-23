package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlockModel;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class InteriorDoorBlockRenderer extends GeoBlockRenderer<@NotNull InteriorDoorBlockEntity, @NotNull TardisExteriorBlockRenderState> {
    public static final DataTicket<String> VARIANT = DataTicket.create("interior_door_variant", String.class);
    public static final DataTicket<String> TEXTURE_ID = DataTicket.create("interior_door_texture_id", String.class);
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
        renderState.addGeckolibData(VARIANT, animatable.getVariant());
        renderState.addGeckolibData(TEXTURE_ID, animatable.getTextureId());
        renderState.addGeckolibData(YAW, animatable.getFacing().toYRot());
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        Float yawValue = renderPassInfo.renderState().getGeckolibData(YAW);
        float yaw = yawValue != null ? yawValue : 0f;
        Direction right = InteriorDoorBlock.rightOf(Direction.fromYRot(yaw));

        renderPassInfo.poseStack().translate(right.getStepX() * 0.5f, 0f, right.getStepZ() * 0.5f);
        renderPassInfo.poseStack().rotateDegrees(Axis.YP, TardisExterior.modelRotationDegrees(yaw));
    }
}
