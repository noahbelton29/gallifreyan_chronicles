package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;

public class PowerControl extends BaseControl {
    public static final ControlAnimations DEFAULT_ANIMATIONS =
            new ControlAnimations("power_control_on", "power_control_off", false);

    public PowerControl(ControlAnimations animations) {
        super(animations, ClickSound.SLIDER);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        if (lockedWhileCrashing(ctx, player, "power_locked_crashing")) return InteractionResult.FAIL;
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void changed(ControlContext ctx, boolean activated) {
        if (!activated) ctx.tardis().ifPresent(TardisAlarm::silence);
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        super.playClickSound(entity);
        TardisConsoleBlockEntity console = entity.getConsole();
        if (console == null) return;
        playSound(
                entity,
                console.getControlState(GCTardisControls.POWER_CONTROL)
                        ? GCSounds.TARDIS_POWER_OFF
                        : GCSounds.TARDIS_POWER_ON,
                1.0f);
    }

    @Override
    public Component describeState(boolean activated) {
        return activated ? state("power", "on", ChatFormatting.GREEN) : state("power", "off", ChatFormatting.RED);
    }
}
