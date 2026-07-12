package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class FlightLeverControl implements TardisControl {

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;

        entity.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1.0f);
        Constants.LOG.info("clicked flight control");
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClick(TardisControlEntity entity, Player player) {
        return true;
    }

    @Override
    public String getAnimation(boolean activated) {
        return activated ? "flight_lever_down" : "flight_lever_up";
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        Constants.LOG.info("Flight lever state changed to: {}", activated);

        if (activated) {
            console.triggerRotorAnimation("flight");
        } else {
            console.triggerRotorAnimation("ideal");
        }
    }
}