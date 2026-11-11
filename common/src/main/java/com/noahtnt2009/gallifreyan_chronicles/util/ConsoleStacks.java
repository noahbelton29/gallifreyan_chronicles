package com.noahtnt2009.gallifreyan_chronicles.util;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import net.minecraft.world.item.ItemStack;

public final class ConsoleStacks {
    private ConsoleStacks() {
    }

    public static ItemStack withConsoleData(ItemStack stack, TardisConsoleBlockEntity console) {
        TardisConsole model = console.getConsole();
        if (model == null) return stack;
        stack.set(GCDataComponents.CONSOLE_ID, model.id());

        String variantId = console.getVariant();
        if (variantId != null && !variantId.isEmpty() && model.hasVariant(variantId)) {
            stack.set(GCDataComponents.CONSOLE_VARIANT, GCUtils.localPath(variantId));
        }
        return stack;
    }
}
