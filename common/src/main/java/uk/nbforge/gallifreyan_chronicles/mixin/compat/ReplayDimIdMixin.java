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
   Flashback makes the TARDIS world partway through a replay and Immersive Portals never gave it a number so the replay server crashed.
   In a replay we give it a number when it is first asked for.
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
        // Only the server list, telling the client would renumber the recorded packets and put TARDIS blocks in the overworld
        DimIntIdMap map = DimensionIntId.getServerMap(server);
        if (!map.containsDimId(dim)) map.add(dim, gc$pickId(map, dim));
        return map.toIntegerId(dim);
    }

    // Use the same number the recording used, the client already knows it from the recording
    // A different number made recorded TARDIS chunks land in the wrong world after jumping back so the interior had the wrong blocks
    @Unique
    private static int gc$pickId(DimIntIdMap serverMap, ResourceKey<Level> dim) {
        DimIntIdMap clientMap = DimensionIntId.getClientMap();
        if (clientMap.containsDimId(dim)) {
            int id = clientMap.toIntegerId(dim);
            if (!serverMap.containsIntId(id)) return id;
        }
        // Not known from the recording so pick a number the recording does not use either
        // yes this looks pointless but without it the interior chunks end up in the wrong world
        int id = serverMap.getNextIntegerId();
        while (serverMap.containsIntId(id) || clientMap.containsIntId(id)) id++;
        return id;
    }
}
