package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import qouteall.imm_ptl.core.portal.Portal;

/**
 * replays can hand the client broken portals, ones where the other side is a tardis dimension that doesnt exist and
 * ones with no shape (axisW is null). both crashed the game so they get removed. no idea why flashback hands these over
 * but whatever
 */
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

    @WrapMethod(method = "getDistanceToNearestPointInPortal", remap = false, require = 0)
    private double gc$noShapeFar(Vec3 pos, Operation<Double> original) {
        if (((Portal) (Object) this).getAxisW() == null) return Double.MAX_VALUE;
        return original.call(pos);
    }
}
