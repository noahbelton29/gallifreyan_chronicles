package uk.nbforge.gallifreyan_chronicles.core.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.core.item.tooltip.ItemInfoTooltip;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class ConsoleBlockItem extends BlockItem implements ScrollSelectable {
    private record Entry(String consoleId, @Nullable String variantId) {}

    public ConsoleBlockItem(Block block, Properties properties) {
        super(block, properties.component(GCDataComponents.ITEM_INFO, ItemInfoTooltip.CONSOLE));
    }

    public static String defaultConsoleId() {
        TardisConsole synced = TardisConsoleRegistry.getDefault();
        return synced != null ? synced.id() : TardisConsoleRegistry.defaultId();
    }

    public static boolean hasChoice(TardisConsole console) {
        return console.variants().size() > 1;
    }

    private static String localId(String id) {
        return GCUtil.localPath(id);
    }

    private static List<Entry> entries() {
        List<TardisConsole> consoles = new ArrayList<>(TardisConsoleRegistry.getAll());
        consoles.sort(Comparator.comparingInt(TardisConsole::sortOrder).thenComparing(TardisConsole::id));

        List<Entry> entries = new ArrayList<>();
        for (TardisConsole console : consoles) {
            if (!hasChoice(console)) {
                entries.add(new Entry(console.id(), null));
            } else {
                List<TardisConsoleVariant> variants = new ArrayList<>(console.variants());
                variants.sort(Comparator.comparingInt(TardisConsoleVariant::sortOrder)
                        .thenComparing(TardisConsoleVariant::localId));
                for (TardisConsoleVariant variant : variants) {
                    entries.add(new Entry(console.id(), variant.localId()));
                }
            }
        }
        return entries;
    }

    public static @Nullable String effectiveVariantId(String consoleId, @Nullable String variantId) {
        if (variantId != null || !TardisConsoleRegistry.contains(consoleId)) return variantId;
        TardisConsole console = TardisConsoleRegistry.get(consoleId);
        if (!hasChoice(console)) return null;
        String fallback = console.resolvedDefaultVariant();
        return fallback.isEmpty() ? null : fallback;
    }

    public static String selectedVariantKey(ItemStack stack) {
        String variantId = stack.get(GCDataComponents.CONSOLE_VARIANT);
        if (variantId != null) return localId(variantId);
        String consoleId = stack.get(GCDataComponents.CONSOLE_ID);
        if (consoleId == null) consoleId = defaultConsoleId();
        return consoleId == null ? "" : localId(consoleId);
    }

    public static int selectionCount() {
        return entries().size();
    }

    /** wraps round at the ends, false if theres nothing to switch between */
    @Override
    public boolean cycleSelection(ItemStack stack, int step) {
        List<Entry> entries = entries();
        if (entries.size() < 2) return false;

        String consoleId = stack.get(GCDataComponents.CONSOLE_ID);
        if (consoleId == null) consoleId = defaultConsoleId();
        String variantId = effectiveVariantId(consoleId, stack.get(GCDataComponents.CONSOLE_VARIANT));

        int index = 0;
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.consoleId().equals(consoleId) && Objects.equals(entry.variantId(), variantId)) {
                index = i;
                break;
            }
        }

        Entry next = entries.get(Math.floorMod(index + step, entries.size()));
        stack.set(GCDataComponents.CONSOLE_ID, next.consoleId());
        if (next.variantId() == null) {
            stack.remove(GCDataComponents.CONSOLE_VARIANT);
        } else {
            stack.set(GCDataComponents.CONSOLE_VARIANT, next.variantId());
        }
        return true;
    }
}
