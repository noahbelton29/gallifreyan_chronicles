package com.noahtnt2009.gallifreyan_chronicles.mixin.worldgen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* vanilla bug MC-271899: structure palettes cache blocks in a plain HashMap. voxy's
   worldgen builds chunks on its own threads at the same time as the game, two threads
   write the map at once and it crashes. neoforge already fixes this, fabric doesn't */
@Mixin(StructureTemplate.Palette.class)
public abstract class StructurePaletteCacheMixin {
    @Shadow @Final @Mutable
    private Map<Block, List<StructureTemplate.StructureBlockInfo>> cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gc$threadSafeCache(CallbackInfo ci) {
        if (!(cache instanceof ConcurrentHashMap)) cache = new ConcurrentHashMap<>();
    }
}
