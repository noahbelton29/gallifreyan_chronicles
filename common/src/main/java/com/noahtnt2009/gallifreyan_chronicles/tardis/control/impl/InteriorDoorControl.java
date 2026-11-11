package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class InteriorDoorControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("interior_door_control_on", "interior_door_control_off", false);
    public static final MapCodec<InteriorDoorControl> CODEC = codec(DEFAULT_ANIMATIONS, InteriorDoorControl::new);

    public InteriorDoorControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public InteriorDoorControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        playSound(entity, GCSounds.SWITCH, 0.8f);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
        }
        if (entity.level() instanceof ServerLevel) {
            Optional<Tardis> tardis = tardis(console);
            if (tardis.isEmpty() || doorsBlocked(tardis.get(), player)) return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!(console.getLevel() instanceof ServerLevel)) return;
        tardis(console).ifPresent(tardis -> {
            InteriorDoorBlockEntity door = DoorSyncSystem.primaryDoor(tardis.id());
            if (door == null) return;
            boolean open = !door.getDoorState().isClosed();
            if (open != activated) {
                door.interact(true);
            }
        });
    }

    @Override
    public MapCodec<InteriorDoorControl> codec() {
        return CODEC;
    }
}
