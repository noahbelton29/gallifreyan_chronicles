package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;

public class NavigationalLockControl extends DestinationControl {
    public NavigationalLockControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    protected boolean blockedByNavigationalLock() {
        return false;
    }

    @Override
    protected boolean apply(Tardis tardis, @Nullable Player player) {
        boolean engaging = !tardis.getControlState(GCTardisControls.NAVIGATIONAL_LOCK_CONTROL);

        if (!engaging) {
            if (player != null) player.sendOverlayMessage(describeState(false));
            return true;
        }

        LandingTarget pending = tardis.data().getPendingLandingTarget();
        if (pending == null) return false;
        tardis.data().setLandingTarget(pending);
        if (player != null) player.sendOverlayMessage(describeState(true));
        return true;
    }

    @Override
    protected void changed(ControlContext ctx, boolean activated) {
        if (!activated) {
            Tardis tardis = ctx.tardis().orElse(null);
            if (tardis == null) return;
            BlockPos currentPos = tardis.exteriorPos().orElse(null);
            if (currentPos == null) return;

            float yaw = tardis.exterior().map(TardisExteriorBlockEntity::getYaw).orElse(0f);
            LandingTarget pendingTarget = new LandingTarget(tardis.exteriorDimension(), currentPos, yaw);
            tardis.data().setPendingLandingTarget(pendingTarget);
            tardis.data().setLandingTarget(null);
        }
    }

    @Override
    public @NonNull Component describeState(boolean activated) {
        return activated
                ? state("navigational_lock_control", "engaged", ChatFormatting.GREEN)
                : state("navigational_lock_control", "disengaged", ChatFormatting.RED);
    }
}
