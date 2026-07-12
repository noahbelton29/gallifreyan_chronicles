package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record TardisConsoleAnimationPayload(BlockPos pos, String animationName, String controllerName) implements CustomPacketPayload {
    public static final Type<TardisConsoleAnimationPayload> TYPE =
            new Type<>(GCUtils.of("tardis_console_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisConsoleAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            TardisConsoleAnimationPayload::pos,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleAnimationPayload::animationName,
            ByteBufCodecs.STRING_UTF8,
            TardisConsoleAnimationPayload::controllerName,
            TardisConsoleAnimationPayload::new
    );

    public TardisConsoleAnimationPayload(BlockPos pos, String animationName) {
        this(pos, animationName, "controls");
    }

    public void apply() {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) return;

        var be = level.getBlockEntity(pos);
        if (be instanceof TardisConsoleBlockEntity console) {
            console.triggerAnimationClient(animationName, controllerName);
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}