package com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl;

import java.util.Optional;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.BaseControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlAnimations;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;


public class DoorControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("door_control_off", "door_control_on", true);
    public static final MapCodec<DoorControl> CODEC = codec(DEFAULT_ANIMATIONS, DoorControl::new);

    public DoorControl() { this(DEFAULT_ANIMATIONS); }
    public DoorControl(ControlAnimations animations) { super(animations); }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        playSound(entity, GCSounds.SWITCH, 0.8f);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;

        if (entity.level() instanceof ServerLevel level) {
            Optional<Tardis> tardis = tardis(console);
            if (tardis.map(Tardis::isLocked).orElse(false)) {
                tell(player, "message.gallifreyan_chronicles.doors_locked");
                return InteractionResult.FAIL;
            }
            if (tardis.map(Tardis::areDoorsSealedForTravel).orElse(false)) {
                DoorSyncSystem.announceSealed(tardis.get().id());
                tell(player, "message.gallifreyan_chronicles.doors_sealed");
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!(console.getLevel() instanceof ServerLevel level)) return;

        tardis(console).ifPresent(tardis -> DoorSyncSystem.toggleAllDoors(level.getServer(), tardis.id()));
    }

    @Override
    public MapCodec<DoorControl> codec() { return CODEC; }
}
