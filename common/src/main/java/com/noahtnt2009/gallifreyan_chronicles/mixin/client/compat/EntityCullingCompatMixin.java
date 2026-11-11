package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.util.TardisDimensions;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Pseudo
@Mixin(targets = "dev.tr7zw.entityculling.EntityCullingModBase", remap = false)
public abstract class EntityCullingCompatMixin {

    @Shadow
    public abstract void addDynamicBlockEntityWhitelist(Function<BlockEntity, Boolean> whitelist);

    @Shadow
    public abstract void addDynamicEntityWhitelist(Function<Entity, Boolean> whitelist);

    @Inject(method = "onInitialize", at = @At("TAIL"), remap = false, require = 0)
    private void gc$registerWhitelists(CallbackInfo ci) {
        addDynamicBlockEntityWhitelist(EntityCullingCompatMixin::gc$keepBlockEntity);
        addDynamicEntityWhitelist(EntityCullingCompatMixin::gc$keepEntity);
    }

    @Unique
    private static boolean gc$keepBlockEntity(BlockEntity blockEntity) {
        Identifier id = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());
        if (id != null && id.getNamespace().equals(Constants.MOD_ID)) {
            return true;
        }
        return gc$notInViewedLevel(blockEntity.getLevel());
    }

    @Unique
    private static boolean gc$keepEntity(Entity entity) {
        if (entity.getClass().getName().startsWith("qouteall.")) {
            return true;
        }
        return gc$notInViewedLevel(entity.level());
    }

    @Unique
    private static boolean gc$notInViewedLevel(Level level) {
        if (level == null) {
            return false;
        }
        if (TardisDimensions.isInterior(level)) {
            return true;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.level() != level;
    }
}
