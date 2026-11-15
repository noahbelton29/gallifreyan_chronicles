package uk.nbforge.gallifreyan_chronicles.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.client.input.GCKeyMappings;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;

public class TardisControlEntityRenderer extends EntityRenderer<TardisControlEntity, EntityRenderState> {
    private static final double TAG_OFFSET = 0.1;
    private static final float TAG_SCALE = 0.3F;

    public TardisControlEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    protected boolean shouldShowName(@NonNull TardisControlEntity entity, double distanceToCameraSq) {
        return GCKeyMappings.controlNamesOn()
                && Minecraft.getInstance().hitResult instanceof EntityHitResult hit
                && hit.getEntity() == entity;
    }

    @Override
    public void extractRenderState(@NonNull TardisControlEntity entity, @NonNull EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state.nameTagAttachment != null) {
            state.nameTagAttachment = state.nameTagAttachment.subtract(0.0, TAG_OFFSET, 0.0);
        }
    }

    @Override
    public void submit(@NonNull EntityRenderState state, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState camera) {
        Vec3 anchor = state.nameTagAttachment;
        if (anchor == null || state.nameTag == null) {
            super.submit(state, poseStack, collector, camera);
            return;
        }

        poseStack.pushPose();
        poseStack.translate(anchor.x * (1 - TAG_SCALE), anchor.y * (1 - TAG_SCALE), anchor.z * (1 - TAG_SCALE));
        poseStack.scale(TAG_SCALE, TAG_SCALE, TAG_SCALE);
        super.submit(state, poseStack, collector, camera);
        poseStack.popPose();
    }
}
