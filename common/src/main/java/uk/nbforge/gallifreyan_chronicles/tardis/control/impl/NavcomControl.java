package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.Control;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;

public class NavcomControl extends Control {
    public NavcomControl(ControlAnimations animations) {
        super(animations, ClickSound.SWITCH);
    }

    @Override
    public @Nullable Component describeState(boolean activated) {
        return activated
                ? state("navcom", "enabled", ChatFormatting.GREEN)
                : state("navcom", "disabled", ChatFormatting.RED);
    }
}
