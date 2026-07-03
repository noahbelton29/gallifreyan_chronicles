package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisExteriorBlockModel;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class TardisExteriorBlockRenderer extends GeoBlockRenderer<@NotNull TardisExteriorBlockEntity, @NotNull TardisExteriorBlockRenderState> {
    public static final DataTicket<String> EXTERIOR_ID = DataTicket.create("exterior_id", String.class);
    public static final DataTicket<Float>  YAW = DataTicket.create("tardis_yaw", Float.class);

    public TardisExteriorBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new TardisExteriorBlockModel());
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
    }

    @Override
    public void submitRenderTasks(RenderPassInfo<TardisExteriorBlockRenderState> renderPassInfo,
                                  @NonNull OrderedSubmitNodeCollector renderTasks,
                                  @Nullable RenderType renderType) {
        Float yaw = renderPassInfo.renderState().getGeckolibData(YAW);
        float actualYaw = yaw != null ? yaw : 0.0f;
        renderPassInfo.poseStack().mulPose(Axis.YP.rotationDegrees(180.0f - actualYaw));
        super.submitRenderTasks(renderPassInfo, renderTasks, renderType);
    }
}