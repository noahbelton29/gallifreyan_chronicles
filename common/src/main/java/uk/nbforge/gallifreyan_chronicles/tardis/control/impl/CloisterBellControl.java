package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class CloisterBellControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS = new ControlAnimations("cloister_bell_control_on", "cloister_bell_control_off", false);

    @Override
    public void playClickSound(TardisControlEntity entity) {
        playSound(entity, GCSounds.SWITCH, 0.8f);
    }

    public CloisterBellControl(ControlAnimations animations) {
        super(animations);
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        var console = entity.getConsole();
        if (console == null) return InteractionResult.FAIL;
        if (linkedTardis(console).map(TardisCrash::isCrashing).orElse(false)) {
            tell(player, "message.gallifreyan_chronicles.cloister_bell_locked_crashing");
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        if (!(console.getLevel() instanceof ServerLevel)) return;
        linkedTardis(console).ifPresent(tardis -> {
            if (activated) {
                TardisAlarm.controlOn(tardis);
            } else {
                TardisAlarm.controlOff(tardis);
            }
        });
    }

    @Override
    public Component describeState(boolean activated) {
        return activated ? state("cloister_bell", "on", ChatFormatting.GREEN) : state("cloister_bell", "off", ChatFormatting.RED);
    }
}
