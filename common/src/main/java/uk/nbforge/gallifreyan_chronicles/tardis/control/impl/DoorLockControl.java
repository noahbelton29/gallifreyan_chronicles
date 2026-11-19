package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.Control;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;

public class DoorLockControl extends Control {
    public DoorLockControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        TardisExteriorBlockEntity exterior =
                ctx.tardis().flatMap(Tardis::exterior).orElse(null);
        if (exterior == null) {
            MessageUtil.overlay(player, ChatFormatting.RED, "lock_no_exterior");
            return InteractionResult.FAIL;
        }

        ctx.console()
                .reconcileControl(
                        GCTardisControls.DOOR_LOCK_CONTROL, exterior.lock().isLocked());

        if (!exterior.lock().isLocked() && !exterior.doors().areFullyClosed()) {
            MessageUtil.overlay(player, ChatFormatting.RED, "close_doors_to_lock");
            return InteractionResult.FAIL;
        }
        MessageUtil.overlay(
                player, ChatFormatting.GREEN, exterior.lock().isLocked() ? "doors_unlocking" : "doors_locking");
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void changed(ControlContext ctx, boolean activated) {
        ctx.tardis()
                .flatMap(Tardis::exterior)
                .ifPresent(exterior -> exterior.lock().setLockedFromConsole(activated));
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("door_lock", "locked", ChatFormatting.RED)
                : state("door_lock", "unlocked", ChatFormatting.GREEN);
    }
}
