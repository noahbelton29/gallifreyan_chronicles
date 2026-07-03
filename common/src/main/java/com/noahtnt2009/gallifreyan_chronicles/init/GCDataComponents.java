package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.Identifier;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import java.util.function.Supplier;

public class GCDataComponents {
    public static final DataComponentType<String> COBBLESTONE_VARIANT = register(
            "cobblestone_variant",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    private static <T> DataComponentType<T> register(String name, Supplier<DataComponentType<T>> supplier) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, name),
                supplier.get()
        );
    }

    public static void registerDataComponents() {
        Constants.LOG.info("Registered GC Data Components");
    }
}