package com.noahtnt2009.gallifreyan_chronicles.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class SelectionTooltip {
    private SelectionTooltip() {
    }

    public static Component line(String labelKey, Component value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(value);
    }

    public static Component highlight(Component name) {
        return Component.empty().append(name).withStyle(ChatFormatting.BLUE);
    }

    public static Component scrollHint() {
        return Component.translatable("tooltip.gallifreyan_chronicles.scroll")
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    public static Component displayName(String kind, String id) {
        int colon = id.indexOf(':');
        String namespace = colon >= 0 ? id.substring(0, colon) : "minecraft";
        String path = colon >= 0 ? id.substring(colon + 1) : id;

        String base = path;
        for (String suffix : new String[]{"_interior", "_exterior"}) {
            if (base.endsWith(suffix)) {
                base = base.substring(0, base.length() - suffix.length());
                break;
            }
        }
        StringBuilder pretty = new StringBuilder();
        for (String word : base.split("_")) {
            if (word.isEmpty()) continue;
            if (!pretty.isEmpty()) pretty.append(' ');
            pretty.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return Component.translatableWithFallback(kind + "." + namespace + "." + path, pretty.toString());
    }
}
