package com.noahtnt2009.gallifreyan_chronicles.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAtlasManagerAccessor {
    @Accessor("atlasManager")
    AtlasManager gallifreyan_chronicles$getAtlasManager();
}