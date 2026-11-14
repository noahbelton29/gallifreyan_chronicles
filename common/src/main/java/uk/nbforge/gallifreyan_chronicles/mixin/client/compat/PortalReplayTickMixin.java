package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import qouteall.imm_ptl.core.portal.Portal;

/* replays can give the client broken portals:
   - the other side is a tardis dimension that doesn't exist
   - the portal has no shape (axisW is null)
   both crashed the game. we remove those portals. */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.portal.Portal", remap = false)
public abstract class PortalReplayTickMixin {
    @WrapMethod(method = "tick", remap = false, require = 0)
    private void gc$dropBrokenPortal(Operation<Void> original) {
        if (((Portal) (Object) this).getAxisW() == null) {
            ((Entity) (Object) this).discard();
            return;
        }
        try {
            original.call();
        } catch (RuntimeException e) {
            if (!String.valueOf(e.getMessage()).startsWith("Cannot create invalid client dimension")) throw e;
            ((Entity) (Object) this).discard();
        }
    }

    /* the renderer asks this before the portal gets to tick. no shape means "very far away" */
    @WrapMethod(method = "getDistanceToNearestPointInPortal", remap = false, require = 0)
    private double gc$noShapeFar(Vec3 pos, Operation<Double> original) {
        if (((Portal) (Object) this).getAxisW() == null) return Double.MAX_VALUE;
        return original.call(pos);
    }
}
