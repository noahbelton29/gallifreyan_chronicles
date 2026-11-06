package com.noahtnt2009.gallifreyan_chronicles.mixin;

import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBarrierBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisDeletionMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeTardisMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @SuppressWarnings({"unused", "resource"})
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void gc$protectTardisThroughPortal(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel redirected = TardisPortalBridge.redirectedActionLevel();
        ServerLevel target = redirected != null ? redirected : level;
        if (target == player.level()) return;
        BlockState state = target.getBlockState(pos);
        Block block = state.getBlock();
        boolean protectedBlock = block instanceof TardisBlock
                || block instanceof TardisTopBlock
                || block instanceof InteriorDoorBarrierBlock
                || block instanceof InteriorDoorBlock door && door.isLinked();
        if (!protectedBlock) return;
        cir.setReturnValue(false);
    }

    @SuppressWarnings({"unused", "resource"})
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void gc$blockTardisDeletionWithoutDeletionMode(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!player.isCreative()) return;
        if (TardisDeletionMode.isEnabled(player.getUUID())) return;

        ServerLevel redirected = TardisPortalBridge.redirectedActionLevel();
        ServerLevel target = redirected != null ? redirected : level;
        BlockState state = target.getBlockState(pos);
        Block block = state.getBlock();

        boolean isExterior = block instanceof TardisBlock;
        boolean isTopOfExterior = block instanceof TardisTopBlock && target.getBlockState(pos.below()).is(GCBlocks.TARDIS);
        if (!isExterior && !isTopOfExterior) return;

        if (TardisDeletionMode.shouldWarn(player.getUUID(), level.getGameTime())) {
            player.sendSystemMessage(Component.translatable(
                    "message.gallifreyan_chronicles.deletion_mode_required",
                    Component.literal("TARDIS PROTECTED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    Component.literal("/gc tardis deletion on").withStyle(ChatFormatting.YELLOW)
            ).withStyle(ChatFormatting.GRAY));
        }
        cir.setReturnValue(false);
    }
}