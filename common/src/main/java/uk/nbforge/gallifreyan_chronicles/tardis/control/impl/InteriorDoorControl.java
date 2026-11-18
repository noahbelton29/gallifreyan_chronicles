package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;

public class InteriorDoorControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("interior_door_control_on", "interior_door_control_off", false);

    public InteriorDoorControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        InteriorDoorBlockEntity door =
                ctx.tardis().flatMap(Tardis::primaryInteriorDoor).orElse(null);
        if (door != null && door.isOnCooldown()) return InteractionResult.FAIL;
        MessageUtil.overlay(
                player,
                ChatFormatting.GREEN,
                ctx.state(entity.getControlId()) ? "interior_door_closing" : "interior_door_opening");
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void changed(ControlContext ctx, boolean activated) {
        ctx.tardis().flatMap(Tardis::primaryInteriorDoor).ifPresent(door -> {
            boolean open = !door.getDoorState().isClosed();
            if (open == activated) return;
            door.interact(true);
            if (door.getDoorState().isClosed() == activated) {
                ctx.console()
                        .setControlActivated(
                                GCTardisControls.INTERIOR_DOOR_CONTROL,
                                !door.getDoorState().isClosed());
            }
        });
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("interior_door", "open", ChatFormatting.GREEN)
                : state("interior_door", "closed", ChatFormatting.RED);
    }
}
