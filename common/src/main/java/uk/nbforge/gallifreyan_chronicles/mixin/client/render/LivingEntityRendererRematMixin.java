package uk.nbforge.gallifreyan_chronicles.mixin.client.render;

import uk.nbforge.gallifreyan_chronicles.client.util.FadeTintUtil;

import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.model.Model;
import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import uk.nbforge.gallifreyan_chronicles.client.tardis.RematAlpha;
import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* a player a tardis materialised around fades in for everyone else, like the tardis */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererRematMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"))
    private void gc$rematAlpha(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        float alpha = 1.0f;
        if (entity instanceof Player player && !(player instanceof LocalPlayer)) {
            /* they fade in for others, and fade with the interior for someone it's materialising around */
            alpha = Math.min(RematCatchClient.playerAlpha(entity.getUUID(), partialTicks),
                    RematCatchClient.opacityIn(entity.level()));
        }
        ((RematAlpha) state).gc$setRematAlpha(alpha);
    }

    /* see-through body while fading */
    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType gc$fadingType(LivingEntityRenderer<?, ?, ?> renderer, LivingEntityRenderState state,
                                     boolean bodyVisible, boolean forceTransparent, boolean glowing,
                                     Operation<RenderType> original) {
        if (((RematAlpha) state).gc$rematAlpha() >= 1.0f || !bodyVisible) {
            return original.call(renderer, state, bodyVisible, forceTransparent, glowing);
        }
        return RenderTypes.entityTranslucentCull(gc$texture(renderer, state));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Identifier gc$texture(LivingEntityRenderer renderer, LivingEntityRenderState state) {
        return renderer.getTextureLocation(state);
    }

    /* fade like the exterior: depth mask first so only the front shows, then the tinted see-through body */
    @WrapOperation(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"),
            require = 0)
    private void gc$fadingModel(SubmitNodeCollector collector, Model<?> model, Object modelState, PoseStack poseStack,
                                RenderType renderType, int light, int overlay, int color, @Nullable UvMapping uv,
                                int outline, Operation<Void> original,
                                @Local(argsOnly = true) LivingEntityRenderState state) {
        float alpha = ((RematAlpha) state).gc$rematAlpha();
        if (alpha >= 1.0f) {
            original.call(collector, model, modelState, poseStack, renderType, light, overlay, color, uv, outline);
            return;
        }
        poseStack.pushPose();
        poseStack.last().pose().scaleLocal(RematCatchClient.MASK_PUSH, RematCatchClient.MASK_PUSH, RematCatchClient.MASK_PUSH);
        gc$submitMask(collector, model, modelState, poseStack, light, overlay, uv);
        poseStack.popPose();
        original.call(collector, model, modelState, poseStack, renderType, light, overlay,
                ARGB.multiply(color, FadeTintUtil.of(alpha)), uv, outline);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void gc$submitMask(SubmitNodeCollector collector, Model model, Object modelState, PoseStack poseStack,
                                      int light, int overlay, @Nullable UvMapping uv) {
        collector.order(RematCatchClient.MASK_ORDER).submitModel(model, modelState, poseStack, RenderTypes.waterMask(),
                light, overlay, -1, uv, 0);
    }

    /* armour and held items would stay solid, hide them until the player is back */
    @ModifyExpressionValue(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;shouldRenderLayers(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    private boolean gc$hideLayers(boolean original, @Local(argsOnly = true) LivingEntityRenderState state) {
        return original && ((RematAlpha) state).gc$rematAlpha() >= 1.0f;
    }
}
