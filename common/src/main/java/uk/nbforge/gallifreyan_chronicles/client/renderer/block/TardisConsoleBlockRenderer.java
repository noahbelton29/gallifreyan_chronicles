package uk.nbforge.gallifreyan_chronicles.client.renderer.block;

import uk.nbforge.gallifreyan_chronicles.client.util.ModelRenderRangeUtil;

import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.math.Axis;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.client.model.TardisConsoleBlockModel;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.client.renderer.geo_layer.TardisConsoleGlowLayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class TardisConsoleBlockRenderer extends GeoBlockRenderer<@NotNull TardisConsoleBlockEntity, @NotNull TardisConsoleBlockRenderState> {
    public static final DataTicket<String> CONSOLE_ID = DataTicket.create("console_id", String.class);
    public static final DataTicket<String> CONSOLE_VARIANT = DataTicket.create("console_variant", String.class);
    public static final DataTicket<String> CONSOLE_TEXTURE_ID = DataTicket.create("console_texture_id", String.class);
    public static final DataTicket<Float> GLOW_TIME = DataTicket.create("console_glow_time", Float.class);
    public static final DataTicket<Float> GLOW_POWER = DataTicket.create("console_glow_power", Float.class);
    public static final DataTicket<Boolean> UPSIDE_DOWN = DataTicket.create("console_upside_down", Boolean.class);

    public TardisConsoleBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new TardisConsoleBlockModel());

        withRenderLayer(TardisConsoleGlowLayer::new);
    }

    @Override
    public @NotNull TardisConsoleBlockRenderState createRenderState() {
        return new TardisConsoleBlockRenderState();
    }

    @Override
    public void addRenderData(@NotNull TardisConsoleBlockEntity animatable, @Nullable Void relatedObject, @NotNull TardisConsoleBlockRenderState renderState, float partialTick) {
        renderState.addGeckolibData(CONSOLE_ID, animatable.getConsole().id());
        renderState.addGeckolibData(CONSOLE_VARIANT, animatable.getVariant());
        renderState.addGeckolibData(CONSOLE_TEXTURE_ID, animatable.getConsole().textureIdFor(animatable.getVariant()));
        renderState.addGeckolibData(GLOW_POWER, animatable.glowLevel());
        renderState.addGeckolibData(UPSIDE_DOWN, ConsoleBlock.isUpsideDown(animatable.getBlockState()));
        if (animatable.getLevel() != null) {
            renderState.addGeckolibData(GLOW_TIME, animatable.getLevel().getGameTime() + partialTick);
        }
    }

    @Override
    public void adjustRenderPose(@NotNull RenderPassInfo<TardisConsoleBlockRenderState> renderPassInfo) {
        super.adjustRenderPose(renderPassInfo);

        if (!renderPassInfo.renderState().getOrDefaultGeckolibData(UPSIDE_DOWN, false)) return;

        var poseStack = renderPassInfo.poseStack();
        poseStack.translate(0d, 0.5d, 0d);
        poseStack.rotateDegrees(Axis.ZP, 180f);
        poseStack.translate(0d, -0.5d, 0d);
    }

    @Override
    public boolean shouldRender(@NotNull TardisConsoleBlockEntity blockEntity, @NotNull Vec3 cameraPos) {
        return ModelRenderRangeUtil.inRange(blockEntity, cameraPos);
    }

    @Override
    public int getViewDistance() {
        return GCConfig.modelRenderDistance();
    }
}