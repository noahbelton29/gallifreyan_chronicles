package com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer;

import com.geckolib.GeckoLibConstants;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public class TardisExteriorKeyLayer<T extends GeoAnimatable & TardisExteriorKeyLayer.TardisExteriorRenderable, O, R extends GeoRenderState>
        extends GeoRenderLayer<@NonNull T, O, R> {

    public static final String KEY_BONE_NAME = "key_to_render";

    public static final DataTicket<ItemStackRenderState> KEY_RENDER_STATE =
            DataTicket.create("tardis_key_render_state", ItemStackRenderState.class);

    private T currentAnimatable;

    private final ItemModelResolver itemModelResolver;

    public TardisExteriorKeyLayer(GeoRenderer<@NonNull T, O, R> renderer, BlockEntityRendererProvider.Context context) {
        super(renderer);
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public void addRenderData(T animatable, @Nullable O relatedObject, @NonNull R renderState, float partialTick) {
        this.currentAnimatable = animatable;

        ItemStack heldKey = animatable.gc$getHeldKeyStack();

        if (heldKey == null || heldKey.isEmpty()) return;

        ItemStackRenderState itemRenderState = RenderUtil.createRenderStateForItem(
                heldKey,
                itemModelResolver,
                ItemDisplayContext.FIXED
        );

        renderState.addGeckolibData(KEY_RENDER_STATE, itemRenderState);
    }

    @Override
    public void addPerBoneRender(RenderPassInfo<R> renderPassInfo, @NonNull BiConsumer<GeoBone, PerBoneRender<R>> consumer) {
        if (!renderPassInfo.willRender()) return;

        R renderState = renderPassInfo.renderState();

        if (!renderState.hasGeckolibData(KEY_RENDER_STATE)) return;

        ItemStackRenderState itemRenderState = renderState.getGeckolibData(KEY_RENDER_STATE);

        if (itemRenderState == null || itemRenderState.isEmpty()) return;

        BakedGeoModel model = renderPassInfo.model();

        model.getBone(KEY_BONE_NAME).ifPresentOrElse(
                bone -> consumer.accept(bone, this::submitKeyRender),
                () -> GeckoLibConstants.LOGGER.error(
                        "TardisExteriorKeyLayer: unable to find bone '{}', skipping key render", KEY_BONE_NAME)
        );
    }

    private void submitKeyRender(RenderPassInfo<R> renderPassInfo, GeoBone bone, SubmitNodeCollector renderTasks) {
        R renderState = renderPassInfo.renderState();

        if (!renderState.hasGeckolibData(KEY_RENDER_STATE)) return;

        ItemStackRenderState itemRenderState = renderState.getGeckolibData(KEY_RENDER_STATE);

        if (itemRenderState == null || itemRenderState.isEmpty()) return;

        cacheKeyBoneWorldPosition(renderPassInfo, bone);

        PoseStack poseStack = renderPassInfo.poseStack();

        poseStack.pushPose();
        poseStack.scale(0.2f, 0.2f, 0.2f);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90f));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90f));

        itemRenderState.submit(
                poseStack,
                renderTasks,
                renderPassInfo.packedLight(),
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }

    private void cacheKeyBoneWorldPosition(RenderPassInfo<R> renderPassInfo, GeoBone bone) {
        if (!(currentAnimatable instanceof TardisExteriorBlockEntity exterior)) {
            return;
        }

        PoseStack poseStack = renderPassInfo.poseStack();

        Vector4f origin = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        origin.mul(poseStack.last().pose());

        BlockPos pos = exterior.getBlockPos();
        Vec3 worldPos = new Vec3(
                pos.getX() + origin.x(),
                pos.getY() + origin.y(),
                pos.getZ() + origin.z()
        );

        exterior.setCachedKeyBoneWorldPos(worldPos);
    }

    public interface TardisExteriorRenderable {
        @Nullable ItemStack gc$getHeldKeyStack();
    }
}