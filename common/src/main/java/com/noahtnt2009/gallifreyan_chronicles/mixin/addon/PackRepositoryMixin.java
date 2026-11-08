package com.noahtnt2009.gallifreyan_chronicles.mixin.addon;

import com.noahtnt2009.gallifreyan_chronicles.addon.AddonPackSource;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Arrays;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, name = "sources")
    private static RepositorySource[] gallifreyan_chronicles$addAddonSource(RepositorySource[] sources) {
        PackType type = null;
        for (RepositorySource source : sources) {
            if (source instanceof AddonPackSource) return sources;
            if (source instanceof FolderRepositorySource folder) {
                type = ((FolderRepositorySourceAccessor) folder).gallifreyan_chronicles$getPackType();
            }
        }
        if (type == null) return sources;

        RepositorySource[] extended = Arrays.copyOf(sources, sources.length + 1);
        extended[sources.length] = new AddonPackSource(type);
        return extended;
    }
}
