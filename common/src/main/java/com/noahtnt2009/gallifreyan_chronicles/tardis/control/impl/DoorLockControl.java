package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class DoorLockControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("door_lock_on", "door_lock_off", false);
    public static final MapCodec<DoorLockControl> CODEC = codec(DEFAULT_ANIMATIONS, DoorLockControl::new);

    public DoorLockControl() { this(DEFAULT_ANIMATIONS); }
    public DoorLockControl(ControlAnimations animations) { super(animations); }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null || !(entity.level() instanceof ServerLevel level)) return InteractionResult.FAIL;

        TardisExteriorBlockEntity exterior = findExterior(console, level);
        if (exterior == null) {
            tell(player, "The door lock has no linked TARDIS exterior!");
            return InteractionResult.FAIL;
        }

        console.reconcileControl(GCTardisControls.DOOR_LOCK_CONTROL, exterior.isLocked());

        if (!exterior.isLocked() && !exterior.areDoorsFullyClosed()) {
            tell(player, "Close both doors before locking!");
            return InteractionResult.FAIL;
        }

        playSound(entity, GCSounds.SWITCH, 0.8f);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!(console.getLevel() instanceof ServerLevel level)) return;

        TardisExteriorBlockEntity exterior = findExterior(console, level);
        if (exterior != null) exterior.setLockedFromConsole(activated);
    }

    @Override
    public MapCodec<DoorLockControl> codec() { return CODEC; }
}
