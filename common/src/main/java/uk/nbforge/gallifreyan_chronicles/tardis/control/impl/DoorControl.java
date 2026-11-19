package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.Control;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;

public class DoorControl extends Control {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("door_control_off", "door_control_on", true);

    public DoorControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        Tardis tardis = ctx.tardis().orElse(null);
        if (tardis != null && doorsBlocked(tardis, player)) return InteractionResult.FAIL;
        MessageUtil.overlay(
                player, ChatFormatting.GREEN, ctx.state(entity.getControlId()) ? "doors_closing" : "doors_opening");
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void changed(ControlContext ctx, boolean activated) {
        ctx.tardis().ifPresent(DoorSync::toggleAllDoors);
    }

    @Override
    public Component describeState(boolean activated) {
        return activated ? state("doors", "open", ChatFormatting.GREEN) : state("doors", "closed", ChatFormatting.RED);
    }
}
