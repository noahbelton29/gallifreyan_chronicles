package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.ReplayComponentIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* reads item component numbers the recording's way while a replay reads its packets.
   see ReplayComponentIds. ordinal 0 = added components, 2 = removed ones
   (ordinal 1 is the component's value) */
@Mixin(targets = "net.minecraft.core.component.DataComponentPatch$3")
public abstract class ReplayComponentIdsMixin {
    @WrapOperation(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/core/component/DataComponentPatch;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/StreamCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0),
            require = 0)
    private Object gc$addedType(StreamCodec<?, ?> codec, Object buf, Operation<Object> original) {
        return read(codec, buf, original);
    }

    @WrapOperation(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Lnet/minecraft/core/component/DataComponentPatch;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/StreamCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 2),
            require = 0)
    private Object gc$removedType(StreamCodec<?, ?> codec, Object buf, Operation<Object> original) {
        return read(codec, buf, original);
    }

    private static Object read(StreamCodec<?, ?> codec, Object buf, Operation<Object> original) {
        if (!ReplayComponentIds.translating()) return original.call(codec, buf);
        return ReplayComponentIds.byRecordedId(((RegistryFriendlyByteBuf) buf).readVarInt());
    }
}
