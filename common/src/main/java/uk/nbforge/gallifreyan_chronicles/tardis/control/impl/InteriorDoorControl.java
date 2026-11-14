
package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class InteriorDoorControl extends BaseControl {

    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("interior_door_control_on", "interior_door_control_off", false);

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
    public InteractionResult onRightClick(
            TardisControlEntity entity,
            Player player,
            InteractionHand hand
    ) {
        if (entity.getConsole() == null) return InteractionResult.FAIL;
        if (entity.level() instanceof ServerLevel) {
            InteriorDoorBlockEntity door = TardisApi.of(entity.getConsole())
                    .map(tardis -> DoorSyncSystem.primaryDoor(tardis.id())).orElse(null);
            if (door != null && door.isOnCooldown()) return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!(console.getLevel() instanceof ServerLevel)) {
            return;
        }

        TardisApi.of(console).ifPresent(tardis -> {
            InteriorDoorBlockEntity door =
                    DoorSyncSystem.primaryDoor(tardis.id());

            if (door == null) {
                return;
            }

            boolean open = !door.getDoorState().isClosed();

            if (open != activated) {
                door.interact(true);
                if (door.getDoorState().isClosed() == activated) {
                    console.setControlActivated(GCTardisControls.INTERIOR_DOOR_CONTROL, !door.getDoorState().isClosed());
                }
            }
        });
    }
}
