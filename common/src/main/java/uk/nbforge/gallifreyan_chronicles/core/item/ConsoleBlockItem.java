package uk.nbforge.gallifreyan_chronicles.core.item;

import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import uk.nbforge.gallifreyan_chronicles.core.item.tooltip.ItemInfoTooltip;

public class ConsoleBlockItem extends BlockItem implements ScrollSelectable {
    private record Entry(String consoleId, @Nullable String variantId) {
    }

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

    private static final List<String> CONSOLE_ORDER = List.of(
            "brachacki_console",
            "second_doctors_console_alt",
            "inferno_console",
            "ambassadors_of_death_console"
    );

    private static final List<String> VARIANT_ORDER = List.of(
            "first_doctors_console",
            "second_doctors_console",
            "tuat_console"
    );

    private static String localId(String id) {
        return GCUtil.localPath(id);
    }

    private static int orderIndex(List<String> order, String localId) {
        int index = order.indexOf(localId);
        return index < 0 ? order.size() : index;
    }

    private static List<Entry> entries() {
        List<TardisConsole> consoles = new ArrayList<>(TardisConsoleRegistry.getAll());
        consoles.sort(Comparator.comparingInt((TardisConsole c) -> orderIndex(CONSOLE_ORDER, localId(c.id())))
                .thenComparing(TardisConsole::id));

        List<Entry> entries = new ArrayList<>();
        for (TardisConsole console : consoles) {
            if (!hasChoice(console)) {
                entries.add(new Entry(console.id(), null));
            } else {
                List<TardisConsoleVariant> variants = new ArrayList<>(console.variants());
                variants.sort(Comparator.comparingInt((TardisConsoleVariant v) -> orderIndex(VARIANT_ORDER, v.localId()))
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
