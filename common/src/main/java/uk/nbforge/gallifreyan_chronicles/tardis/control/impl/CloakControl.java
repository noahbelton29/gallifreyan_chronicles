package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class CloakControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("cloak_control_on", "cloak_control_off", true);

    public CloakControl(ControlAnimations animations) {
        super(animations);
    }

    public CloakControl() {
        this(DEFAULT_ANIMATIONS);
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        playSound(entity, GCSounds.LEVER, 0.8f);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        var console = entity.getConsole();
        if (console == null) {
            return InteractionResult.FAIL;
        }
        SoundEvent cloakSound = console.getControlState(GCTardisControls.CLOAK_CONTROL) ? GCSounds.TARDIS_UNCLOAK : GCSounds.TARDIS_CLOAK;
        playEverywhere(console, serverLevel, cloakSound);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        linkedTardis(console).flatMap(Tardis::exterior).ifPresent(exterior -> exterior.setCloaked(activated));
    }
}
