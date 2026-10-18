package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisExteriorBlockModel;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorGlowLayer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorKeyLayer;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class TardisExteriorBlockRenderer extends GeoBlockRenderer<@NotNull TardisExteriorBlockEntity, @NotNull TardisExteriorBlockRenderState> {
    public static final DataTicket<String> EXTERIOR_ID = DataTicket.create("exterior_id", String.class);
    public static final DataTicket<Float>  YAW = DataTicket.create("tardis_yaw", Float.class);
    public static final DataTicket<Boolean> GLOWING = DataTicket.create("tardis_glowing", Boolean.class);

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
