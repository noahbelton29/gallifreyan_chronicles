package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisDrumShakePayload;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class FlightLeverControl implements TardisControl {
    private static final int TAKEOFF_SOUND_DELAY_TICKS = 0;
    private static final int TAKEOFF_SOUND_LENGTH_TICKS = 425;

    private boolean STATE = false;

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
        }

        SoundEvent sound = GCSounds.SLIDER;

        entity.level().playSound(
                null,
                entity.blockPosition(),
                sound,
                SoundSource.BLOCKS,
                1.0F,
                STATE ? 1.0f : 0.9f
        );

        boolean takingOff = !console.getControlState(entity.getControlId());
        if (takingOff && entity.level() instanceof ServerLevel level) {
            BlockPos pos = entity.blockPosition();

            level.playSound(null, pos, GCSounds.TARDIS_DRUM, SoundSource.BLOCKS, 1.0F, 1.0F);
            TardisDrumShakePayload.broadcast(level, entity.position(), 1.0F);

            level.playSound(null, pos, GCSounds.TARDIS_TAKEOFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            playAfter(level, pos, TAKEOFF_SOUND_DELAY_TICKS + TAKEOFF_SOUND_LENGTH_TICKS, GCSounds.TARDIS_VWORP);
        }

        Constants.LOG.info("clicked flight control");
        return InteractionResult.SUCCESS;
    }

    private static void playAfter(ServerLevel level, BlockPos pos, int delayTicks, SoundEvent sound) {
        if (delayTicks <= 0) {
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return;
        }
        GCScheduler.runLater(level.getServer(), delayTicks, () ->
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F));
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

        STATE = activated;

        if (console.getLevel() instanceof ServerLevel serverLevel) {
            console.getLinkedTardisId().ifPresent(tardisId -> {
                TardisExteriorBlockEntity exterior = DoorSyncSystem.findExterior(serverLevel.getServer(), tardisId);
                if (exterior != null) {
                    exterior.setInFlight(activated);
                }
            });
        }

        if (activated) {
            console.triggerRotorAnimation("rotor_flight");
        } else {
            console.triggerRotorAnimation("rotor_idle");
        }
    }
}