package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisControlHitboxDebugRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.debug.DebugValueAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {
    @Unique
    private TardisControlHitboxDebugRenderer gallifreyan_chronicles$controlHitboxRenderer;

    @Inject(method = "emitGizmos", at = @At("HEAD"))
    private void gallifreyan_chronicles$emitControlHitboxes(Frustum frustum, double camX, double camY, double camZ, float partialTicks, CallbackInfo ci) {
        if (this.gallifreyan_chronicles$controlHitboxRenderer == null) {
            this.gallifreyan_chronicles$controlHitboxRenderer = new TardisControlHitboxDebugRenderer(Minecraft.getInstance());
        }
        DebugValueAccess debugValues = Objects.requireNonNull(Minecraft.getInstance().getConnection()).createDebugValueAccess();
        this.gallifreyan_chronicles$controlHitboxRenderer.emitGizmos(camX, camY, camZ, debugValues, frustum, partialTicks);
    }
}