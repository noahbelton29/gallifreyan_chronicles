package uk.nbforge.gallifreyan_chronicles.mixin.client.sky;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererTextureMixin {
    @Shadow
    private CloudRenderer.TextureData texture;

    @Unique
    private boolean gallifreyan_Chronicles$gcTextureLoadAttempted;

    @Shadow
    protected abstract Optional<CloudRenderer.TextureData> prepare(ResourceManager resourceManager, ProfilerFiller profiler);

    @Shadow
    protected abstract void apply(Optional<CloudRenderer.TextureData> data, ResourceManager resourceManager, ProfilerFiller profiler);

    @Inject(method = "prepare(ILnet/minecraft/client/CloudStatus;FILnet/minecraft/world/phys/Vec3;JF)V", at = @At("HEAD"))
    private void gcEnsureTexture(int color, CloudStatus status, float height, int renderDistance, Vec3 cameraPos,
                                 long gameTime, float partialTick, CallbackInfo ci) {
        if (texture != null || gallifreyan_Chronicles$gcTextureLoadAttempted) return;
        gallifreyan_Chronicles$gcTextureLoadAttempted = true;
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        ProfilerFiller profiler = Profiler.get();
        apply(prepare(resourceManager, profiler), resourceManager, profiler);
    }
}
