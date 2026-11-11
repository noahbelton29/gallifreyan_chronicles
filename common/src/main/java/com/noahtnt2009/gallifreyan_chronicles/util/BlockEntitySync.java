package com.noahtnt2009.gallifreyan_chronicles.util;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class BlockEntitySync {
    private BlockEntitySync() {
    }

    public static void sendToClients(BlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null) return;
        blockEntity.setChanged();
        level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(),
                Block.UPDATE_ALL);
    }
}
