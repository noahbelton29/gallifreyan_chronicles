package com.noahtnt2009.gallifreyan_chronicles.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public enum ItemInfoTooltip implements TooltipProvider {
    TARDIS,
    CONSOLE,
    KEY;

    public static final Codec<ItemInfoTooltip> CODEC = Codec.STRING.comapFlatMap(
            name -> {
                try {
                    return DataResult.success(valueOf(name.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown item info tooltip: " + name);
                }
            },
            kind -> kind.name().toLowerCase(Locale.ROOT)
    );

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag,
                             DataComponentGetter components) {
        switch (this) {
            case TARDIS -> addTardis(tooltip, flag, components);
            case CONSOLE -> addConsole(tooltip, flag, components);
            case KEY -> addKey(tooltip, components);
        }
    }

    private static void addTardis(Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        String interiorId = TardisInteriorOptions.effectiveInteriorId(components.get(GCDataComponents.TARDIS_INTERIOR));
        if (!interiorId.isEmpty()) {
            tooltip.accept(SelectionTooltip.line("tooltip.gallifreyan_chronicles.tardis.schema",
                    SelectionTooltip.highlight(SelectionTooltip.displayName("tardis_interior", interiorId))));
        }

        if (flag.isCreative() && TardisInteriorOptions.client().size() > 1) {
            tooltip.accept(SelectionTooltip.scrollHint());
        }
    }

    private static void addConsole(Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        String consoleId = components.get(GCDataComponents.CONSOLE_ID);
        if (consoleId == null) consoleId = ConsoleBlockItem.defaultConsoleId();
        String variantId = components.get(GCDataComponents.CONSOLE_VARIANT);

        if (consoleId != null && !consoleId.isEmpty()) {
            tooltip.accept(SelectionTooltip.line("tooltip.gallifreyan_chronicles.console.model",
                    SelectionTooltip.highlight(SelectionTooltip.displayName("tardis_console", consoleId))));

            boolean choice = TardisConsoleRegistry.contains(consoleId)
                    && ConsoleBlockItem.hasChoice(TardisConsoleRegistry.get(consoleId));
            Component value;
            if (variantId == null || !choice) {
                value = Component.translatable("tooltip.gallifreyan_chronicles.console.variant_default")
                        .withStyle(ChatFormatting.GRAY);
            } else {
                TardisConsoleVariant variant = TardisConsoleRegistry.getVariant(variantId);
                String fullId = variant != null ? variant.id() : Constants.MOD_ID + ":" + variantId;
                value = SelectionTooltip.highlight(SelectionTooltip.displayName("tardis_console_variant", fullId));
            }
            tooltip.accept(SelectionTooltip.line("tooltip.gallifreyan_chronicles.console.variant", value));
        }

        if (flag.isCreative() && ConsoleBlockItem.selectionCount() > 1) {
            tooltip.accept(SelectionTooltip.scrollHint());
        }
    }

    private static void addKey(Consumer<Component> tooltip, DataComponentGetter components) {
        UUID tardisId = components.get(GCDataComponents.TARDIS_ID);
        if (tardisId == null) {
            tooltip.accept(Component.translatable("tooltip.gallifreyan_chronicles.key.unlinked")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(SelectionTooltip.line("tooltip.gallifreyan_chronicles.key.linked",
                    SelectionTooltip.highlight(Component.literal(tardisId.toString()))));
        }
    }
}
