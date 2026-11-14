package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class DoorLockControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("door_lock_on", "door_lock_off", false);

    public DoorLockControl() {
        this(DEFAULT_ANIMATIONS);
    }

    public DoorLockControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        playSound(entity, GCSounds.SWITCH, 0.8f);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null || !(entity.level() instanceof ServerLevel level)) return InteractionResult.FAIL;

        TardisExteriorBlockEntity exterior = linkedTardis(console).flatMap(Tardis::exterior).orElse(null);
        if (exterior == null) {
            tell(player, "message.gallifreyan_chronicles.lock_no_exterior");
            return InteractionResult.FAIL;
        }

        console.reconcileControl(GCTardisControls.DOOR_LOCK_CONTROL, exterior.isLocked());

        if (!exterior.isLocked() && !exterior.areDoorsFullyClosed()) {
            tell(player, "message.gallifreyan_chronicles.close_doors_to_lock");
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!(console.getLevel() instanceof ServerLevel level)) return;

        linkedTardis(console).flatMap(Tardis::exterior).ifPresent(exterior -> exterior.setLockedFromConsole(activated));
    }
}
