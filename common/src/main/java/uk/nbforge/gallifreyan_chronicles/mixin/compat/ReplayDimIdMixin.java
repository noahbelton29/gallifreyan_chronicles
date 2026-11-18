package uk.nbforge.gallifreyan_chronicles.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import qouteall.q_misc_util.dimension.DimIntIdMap;
import qouteall.q_misc_util.dimension.DimensionIntId;

/**
 * flashback makes the tardis world partway thru a replay and immersive portals never gave it a number so the replay
 * server crashed. in a replay it gets one the first time its asked for
 */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.api.PortalAPI", remap = false)
public abstract class ReplayDimIdMixin {
    @WrapMethod(method = "serverDimKeyToInt", remap = false, require = 0)
    private static int gc$addMissingDim(MinecraftServer server, ResourceKey<Level> dim, Operation<Integer> original) {
        try {
            return original.call(server, dim);
        } catch (RuntimeException e) {
            if (!String.valueOf(e.getMessage()).startsWith("Missing Dimension")
                    || !server.getClass().getName().startsWith("com.moulberry.flashback.")) throw e;
        }
        DimIntIdMap map = DimensionIntId.getServerMap(server);
        if (!map.containsDimId(dim)) map.add(dim, gc$pickId(map, dim));
        return map.toIntegerId(dim);
    }

    @Unique
    private static int gc$pickId(DimIntIdMap serverMap, ResourceKey<Level> dim) {
        DimIntIdMap clientMap = DimensionIntId.getClientMap();
        if (clientMap.containsDimId(dim)) {
            int id = clientMap.toIntegerId(dim);
            if (!serverMap.containsIntId(id)) return id;
        }
        int id = serverMap.getNextIntegerId();
        while (serverMap.containsIntId(id) || clientMap.containsIntId(id)) id++;
        return id;
    }
}
