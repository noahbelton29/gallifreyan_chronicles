package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.Control;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public class IsomorphicControl extends Control {
    private static final int SECOND_SOUND_DELAY_TICKS = 10;

    public IsomorphicControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        TardisConsoleBlockEntity console = entity.getConsole();
        if (console == null || !(entity.level() instanceof ServerLevel level)) return;
        float pitch = console.getControlState(entity.getControlId()) ? 1.0f : ClickSound.SWITCH.offPitch();
        BlockPos pos = entity.blockPosition();
        ConsoleSounds.at(level, pos, GCSounds.SWITCH, pitch);
        SchedulerUtil.runLater(
                level.getServer(),
                SECOND_SOUND_DELAY_TICKS,
                () -> ConsoleSounds.at(level, pos, GCSounds.SWITCH, pitch));
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("isomorphic", "engaged", ChatFormatting.GREEN)
                : state("isomorphic", "disengaged", ChatFormatting.RED);
    }
}
