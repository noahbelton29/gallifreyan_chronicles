package com.noahtnt2009.gallifreyan_chronicles.init;

import java.util.List;
import java.util.UUID;

import com.noahtnt2009.gallifreyan_chronicles.item.ItemInfoTooltip;
import net.minecraft.core.UUIDUtil;
import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentType;
import com.noahtnt2009.gallifreyan_chronicles.Constants;

import java.util.function.Supplier;

public class GCDataComponents {
    public static final DataComponentType<String> COBBLESTONE_VARIANT = register(
            "cobblestone_variant",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<String> CONSOLE_ID = register(
            "console_id",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<String> CONSOLE_VARIANT = register(
            "console_variant",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<String> TARDIS_INTERIOR = register(
            "tardis_interior",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<String> DOOR_TYPE = register(
            "door_type",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<ItemInfoTooltip> ITEM_INFO = register(
            "item_info",
            () -> DataComponentType.<ItemInfoTooltip>builder()
                    .persistent(ItemInfoTooltip.CODEC).build()
    );

    public static final DataComponentType<UUID> TARDIS_ID = register(
            "tardis_id",
            () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).build()
    );

    public static final DataComponentType<Boolean> KEY_INSERTED = register(
            "key_inserted",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static final DataComponentType<String> KEY_STYLE = register(
            "key_style",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).build()
    );

    public static final DataComponentType<Boolean> MASTER_KEY = register(
            "master_key",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build()
    );

    public static final DataComponentType<List<String>> KEY_TRUSTED = register(
            "key_trusted",
            () -> DataComponentType.<List<String>>builder().persistent(Codec.STRING.listOf()).build()
    );

    private static <T> DataComponentType<T> register(String name, Supplier<DataComponentType<T>> supplier) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                GCUtils.modIdentifier(name),
                supplier.get()
        );
    }

    public static void registerDataComponents() {
        Constants.LOG.info("Registered GC Data Components");
    }
}