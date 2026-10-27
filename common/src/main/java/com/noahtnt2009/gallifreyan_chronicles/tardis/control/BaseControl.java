package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
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

import java.util.UUID;
import java.util.function.Function;

public abstract class BaseControl implements TardisControl {
    private final ControlAnimations animations;

    protected BaseControl(ControlAnimations animations) {
        this.animations = animations;
    }

    protected static <T extends BaseControl> MapCodec<T> codec(ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        return ControlAnimations.codec(defaults).xmap(ctor, BaseControl::animations);
    }

    public ControlAnimations animations() {
        return animations;
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
        playSound(entity, GCSounds.SLIDER, 0.9f);
        return InteractionResult.SUCCESS;
    }

    protected static void playSound(TardisControlEntity entity, SoundEvent sound, float offPitch) {
        TardisConsoleBlockEntity console = entity.getConsole();
        boolean on = console != null && console.getControlState(entity.getControlId());
        entity.level().playSound(null, entity.blockPosition(), sound, SoundSource.BLOCKS, 1.0F, on ? 1.0F : offPitch);
    }

    protected static void playAt(ServerLevel level, BlockPos pos, SoundEvent sound, float pitch) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, pitch);
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
        TardisExteriorBlockEntity exterior = findExterior(console, level);
        if (exterior == null || !(exterior.getLevel() instanceof ServerLevel exteriorLevel)) return;
        playAt(exteriorLevel, exterior.getBlockPos(), sound, 1.0F);
    }

    protected static void playEverywhere(TardisConsoleBlockEntity console, ServerLevel level, SoundEvent sound) {
        playAtAllConsoles(console, level, sound);
        playAtExterior(console, level, sound);
    }

    protected static void tell(@Nullable Player player, String message) {
        if (player != null) player.sendOverlayMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    protected static @Nullable UUID tardisId(TardisConsoleBlockEntity console, ServerLevel level) {
        UUID id = console.getTardisId();
        return id != null ? id : DoorSyncSystem.tardisIdOf(level);
    }

    protected static @Nullable TardisExteriorBlockEntity findExterior(TardisConsoleBlockEntity console, ServerLevel level) {
        UUID id = tardisId(console, level);
        return id == null ? null : DoorSyncSystem.findExterior(level.getServer(), id);
    }
}
