package uk.nbforge.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;

import java.util.function.Function;

/**
 * use and changed only ever run on the server with a console
 */
public abstract class Control implements TardisControl {
    private final ControlAnimations animations;
    private final ClickSound clickSound;

    protected Control(ControlAnimations animations, ClickSound clickSound) {
        this.animations = animations;
        this.clickSound = clickSound;
    }

    protected Control(ControlAnimations animations) {
        this(animations, ClickSound.SLIDER);
    }

    public static <T extends Control> MapCodec<T> codec(
        ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        return ControlAnimations.codec(defaults).xmap(ctor, Control::animations);
    }

    protected static void playSound(TardisControlEntity entity, SoundEvent sound, float offPitch) {
        TardisConsoleBlockEntity console = entity.getConsole();
        boolean on = console != null && console.getControlState(entity.getControlId());
        float pitch = on ? 1.0f : offPitch;
        if (entity.level() instanceof ServerLevel serverLevel) {
            ConsoleSounds.at(serverLevel, entity.blockPosition(), sound, pitch);
        } else {
            entity.level().playSound(null, entity.blockPosition(), sound, SoundSource.BLOCKS, 1.0f, pitch);
        }
    }

    protected static Component state(String label, String value, ChatFormatting colour) {
        return Component.translatable("tooltip.gallifreyan_chronicles.control_state.label." + label)
            .withStyle(ChatFormatting.GRAY)
            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
            .append(Component.translatable("tooltip.gallifreyan_chronicles.control_state.value." + value)
                .withStyle(colour));
    }

    protected static boolean lockedWhileCrashing(ControlContext ctx, @Nullable Player player, String message) {
        if (!ctx.tardis().map(TardisCrash::isCrashing).orElse(false)) return false;
        MessageUtil.overlay(player, ChatFormatting.RED, message);
        return true;
    }

    protected static boolean doorsBlocked(Tardis tardis, @Nullable Player player) {
        if (tardis.isLocked()) {
            MessageUtil.overlay(player, ChatFormatting.RED, "doors_locked");
            return true;
        }
        if (tardis.exterior().map(exterior -> exterior.lock().hasKeyRendered()).orElse(false)) {
            MessageUtil.overlay(player, ChatFormatting.RED, "doors_key_in_lock");
            return true;
        }
        if (tardis.areDoorsSealedForTravel()) {
            DoorSync.announceSealed(tardis);
            MessageUtil.overlay(player, ChatFormatting.RED, "doors_sealed");
            return true;
        }
        return false;
    }

    public ControlAnimations animations() {
        return animations;
    }

    @Override
    public MapCodec<? extends TardisControl> codec() {
        return TardisControls.codecFor(this);
    }

    @Override
    public String getAnimation(boolean activated) {
        return activated ? animations.active() : animations.inactive();
    }

    @Override
    public boolean snapsWhenInactive() {
        return animations.snapsWhenInactive();
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, @Nullable Player player, InteractionHand hand) {
        ControlContext ctx = ControlContext.of(entity);
        if (ctx == null) return InteractionResult.FAIL;
        return use(ctx, entity, player);
    }

    protected InteractionResult use(ControlContext ctx, TardisControlEntity entity, @Nullable Player player) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClick(TardisControlEntity entity, Player player) {
        return true;
    }

    @Override
    public void onStateChanged(TardisConsoleBlockEntity console, boolean activated) {
        ControlContext ctx = ControlContext.of(console);
        if (ctx != null) changed(ctx, activated);
    }

    protected void changed(ControlContext ctx, boolean activated) {
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        SoundEvent sound = clickSound.sound();
        if (sound != null) playSound(entity, sound, clickSound.offPitch());
    }
}
