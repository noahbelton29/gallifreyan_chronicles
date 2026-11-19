package uk.nbforge.gallifreyan_chronicles.tardis.control.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ClickSound;
import uk.nbforge.gallifreyan_chronicles.tardis.control.Control;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlContext;

public class CloakingDeviceControl extends Control {
    public CloakingDeviceControl(ControlAnimations animations) {
        super(animations, ClickSound.LEVER);
    }

    @Override
    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        SoundEvent cloakSound =
                ctx.state(GCTardisControls.CLOAKING_DEVICE_CONTROL) ? GCSounds.TARDIS_UNCLOAK : GCSounds.TARDIS_CLOAK;
        ConsoleSounds.everywhere(ctx.console(), cloakSound);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void changed(ControlContext ctx, boolean activated) {
        ctx.tardis()
                .flatMap(Tardis::exterior)
                .ifPresent(exterior -> exterior.travel().setCloaked(activated));
    }

    @Override
    public Component describeState(boolean activated) {
        return activated
                ? state("cloak", "active", ChatFormatting.GREEN)
                : state("cloak", "inactive", ChatFormatting.RED);
    }
}
