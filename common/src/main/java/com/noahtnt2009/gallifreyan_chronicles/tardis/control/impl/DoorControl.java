package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class DoorControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("door_control_off", "door_control_on", true);
    public static final MapCodec<DoorControl> CODEC = codec(DEFAULT_ANIMATIONS, DoorControl::new);

    public DoorControl() { this(DEFAULT_ANIMATIONS); }
    public DoorControl(ControlAnimations animations) { super(animations); }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;

        if (entity.level() instanceof ServerLevel level) {
            TardisExteriorBlockEntity exterior = findExterior(console, level);
            if (exterior != null && exterior.isLocked()) {
                tell(player, "The doors are locked!");
                return InteractionResult.FAIL;
            }
            if (exterior != null && exterior.areDoorsSealedForTravel()) {
                DoorSyncSystem.announceSealed(tardisId(console, level));
                tell(player, "The doors are sealed while the TARDIS is travelling!");
                return InteractionResult.FAIL;
            }
        }

        playSound(entity, GCSounds.SWITCH, 0.8f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!(console.getLevel() instanceof ServerLevel level)) return;

        UUID tardisId = tardisId(console, level);
        if (tardisId != null) DoorSyncSystem.toggleAllDoors(level.getServer(), tardisId);
    }

    @Override
    public MapCodec<DoorControl> codec() { return CODEC; }
}
