
package uk.nbforge.gallifreyan_chronicles.tardis.control;

import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

/**
 * Base class for TARDIS console controls.
 * Provides shared animations, sounds and door-related helpers.
 */
public abstract class BaseControl implements TardisControl {

    private final ControlAnimations animations;

    /**
     * Creates a control with the given animations.
     *
     * @param animations the animations used by the control
     */
    protected BaseControl(ControlAnimations animations) {
        this.animations = animations;
    }

    /**
     * Creates a codec for controls that use animation settings.
     *
     * @param defaults the default animations
     * @param ctor     creates a control from its animations
     * @param <T>      the control type
     * @return the codec for the control
     */
    public static <T extends BaseControl> MapCodec<T> codec(ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        return ControlAnimations.codec(defaults).xmap(ctor, BaseControl::animations);
    }

    /**
     * Gets the animations used by this control.
     *
     * @return the control's animations
     */
    public ControlAnimations animations() {
        return animations;
    }

    @Override
    public MapCodec<? extends TardisControl> codec() {
        return TardisControlTypes.codecFor(this);
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
    public boolean onLeftClick(TardisControlEntity entity, Player player) {
        return true;
    }

    @Override
    public InteractionResult onRightClick(TardisControlEntity entity, Player player, InteractionHand hand) {
        if (entity.getConsole() == null) return InteractionResult.FAIL;
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playClickSound(TardisControlEntity entity) {
        playSound(entity, GCSounds.SLIDER, 0.9F);
    }

    /**
     * Plays a sound at the control.
     * Uses a different pitch depending on whether the control is active.
     *
     * @param entity   the control entity
     * @param sound    the sound to play
     * @param offPitch the pitch when the control is inactive
     */
    protected static void playSound(TardisControlEntity entity, SoundEvent sound, float offPitch) {
        TardisConsoleBlockEntity console = entity.getConsole();
        boolean on = console != null && console.getControlState(entity.getControlId());
        float pitch = on ? 1.0F : offPitch;

        if (entity.level() instanceof ServerLevel serverLevel) {
            TimedSounds.play(serverLevel, entity.blockPosition(), sound, SoundSource.BLOCKS, 1.0F, pitch);
        } else {
            entity.level().playSound(null, entity.blockPosition(), sound, SoundSource.BLOCKS, 1.0F, pitch);
        }
    }

    /**
     * Plays a sound at a specific position.
     *
     * @param level the level to play the sound in
     * @param pos   the position to play the sound at
     * @param sound the sound to play
     * @param pitch the pitch of the sound
     */
    protected static void playAt(ServerLevel level, BlockPos pos, SoundEvent sound, float pitch) {
        TimedSounds.play(level, pos, sound, SoundSource.BLOCKS, 1.0F, pitch);
    }

    /**
     * Plays a sound at the console.
     *
     * @param console the console to play the sound at
     * @param level   the server level
     * @param sound   the sound to play
     */
    protected static void playAtConsole(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        playAt(level, console.getBlockPos(), sound, 1.0F);
    }

    /**
     * Plays a sound at every connected console.
     *
     * @param console the console used to find connected consoles
     * @param level   the fallback server level
     * @param sound   the sound to play
     */
    protected static void playAtAllConsoles(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        for (TardisConsoleBlockEntity each : ConsoleSyncSystem.liveConsolesWith(console)) {
            playAt(each.getLevel() instanceof ServerLevel eachLevel ? eachLevel : level, each.getBlockPos(), sound, 1.0F);
        }
    }

    /**
     * Plays a sound at the TARDIS exterior, if it exists.
     *
     * @param console the console used to find the TARDIS
     * @param level   the fallback server level
     * @param sound   the sound to play
     */
    protected static void playAtExterior(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        linkedTardis(console).flatMap(Tardis::exterior).ifPresent(exterior -> {
            if (exterior.getLevel() instanceof ServerLevel exteriorLevel) {
                playAt(exteriorLevel, exterior.getBlockPos(), sound, 1.0F);
            }
        });
    }

    /**
     * Plays a sound at every connected console and the TARDIS exterior.
     *
     * @param console the console used to find the TARDIS
     * @param level   the fallback server level
     * @param sound   the sound to play
     */
    protected static void playEverywhere(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        playAtAllConsoles(console, level, sound);
        playAtExterior(console, level, sound);
    }

    /**
     * Sends a red message above the player's hotbar.
     *
     * @param player         the player to send the message to
     * @param translationKey the translation key of the message
     */
    protected static void tell(@Nullable Player player, String translationKey) {
        if (player != null) {
            player.sendOverlayMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Checks whether the TARDIS doors are locked or sealed for travel.
     *
     * @param tardis the TARDIS to check
     * @param player the player to notify if the doors are blocked
     * @return true if the doors cannot be opened
     */
    protected static boolean doorsBlocked(Tardis tardis, @Nullable Player player) {
        if (tardis.isLocked()) {
            tell(player, "message.gallifreyan_chronicles.doors_locked");
            return true;
        }

        if (tardis.areDoorsSealedForTravel()) {
            DoorSyncSystem.announceSealed(tardis.id());
            tell(player, "message.gallifreyan_chronicles.doors_sealed");
            return true;
        }

        return false;
    }

    /**
     * Gets the TARDIS linked to a console.
     *
     * @param console the console to get the TARDIS from
     * @return the linked TARDIS, if one exists
     */
    protected static Optional<Tardis> linkedTardis(TardisConsoleBlockEntity console) {
        return TardisApi.of(console);
    }
}
