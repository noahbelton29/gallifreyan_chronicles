
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

/** Base class for console controls with shared animations, sounds and door helpers. */
public abstract class BaseControl implements TardisControl {

    private final ControlAnimations animations;

    protected BaseControl(ControlAnimations animations) {
        this.animations = animations;
    }

    public static <T extends BaseControl> MapCodec<T> codec(ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        return ControlAnimations.codec(defaults).xmap(ctor, BaseControl::animations);
    }

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

    /** The pitch depends on whether the control is active. */
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

    protected static void playAt(ServerLevel level, BlockPos pos, SoundEvent sound, float pitch) {
        TimedSounds.play(level, pos, sound, SoundSource.BLOCKS, 1.0F, pitch);
    }

    protected static void playAtConsole(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        playAt(level, console.getBlockPos(), sound, 1.0F);
    }

    protected static void playAtAllConsoles(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        for (TardisConsoleBlockEntity each : ConsoleSyncSystem.liveConsolesWith(console)) {
            playAt(each.getLevel() instanceof ServerLevel eachLevel ? eachLevel : level, each.getBlockPos(), sound, 1.0F);
        }
    }

    protected static void playAtExterior(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        linkedTardis(console).flatMap(Tardis::exterior).ifPresent(exterior -> {
            if (exterior.getLevel() instanceof ServerLevel exteriorLevel) {
                playAt(exteriorLevel, exterior.getBlockPos(), sound, 1.0F);
            }
        });
    }

    protected static void playEverywhere(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        playAtAllConsoles(console, level, sound);
        playAtExterior(console, level, sound);
    }

    /** A red message above the hotbar. */
    protected static void announce(@Nullable Player player, String translationKey) {
        if (player != null) {
            player.sendOverlayMessage(Component.translatable(translationKey).withStyle(ChatFormatting.GREEN));
        }
    }

    protected static Component state(String label, String value, ChatFormatting colour) {
        return Component.translatable("tooltip.gallifreyan_chronicles.control_state.label." + label)
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("tooltip.gallifreyan_chronicles.control_state.value." + value).withStyle(colour));
    }

    protected static void tell(@Nullable Player player, String translationKey) {
        if (player != null) {
            player.sendOverlayMessage(Component.translatable(translationKey).withStyle(ChatFormatting.RED));
        }
    }

    /** True if the doors are locked or sealed for travel, it tells the player if so. */
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

    protected static Optional<Tardis> linkedTardis(TardisConsoleBlockEntity console) {
        return TardisApi.of(console);
    }
}
