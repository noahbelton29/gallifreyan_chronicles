package uk.nbforge.gallifreyan_chronicles.util;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import net.minecraft.world.item.ItemStack;

public final class ConsoleStackUtil {
    private ConsoleStackUtil() {
    }

    public static ItemStack withConsoleData(ItemStack stack, TardisConsoleBlockEntity console) {
        TardisConsole model = console.getConsole();
        if (model == null) return stack;
        stack.set(GCDataComponents.CONSOLE_ID, model.id());

        String variantId = console.getVariant();
        if (variantId != null && !variantId.isEmpty() && model.hasVariant(variantId)) {
            stack.set(GCDataComponents.CONSOLE_VARIANT, GCUtil.localPath(variantId));
        }
        return stack;
    }
}
