package uk.nbforge.gallifreyan_chronicles.addon;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Items addons registered, for the addons tab. */
public final class AddonContent {
    private static final List<Item> TAB_ITEMS = Collections.synchronizedList(new ArrayList<>());

    private AddonContent() {
    }

    public static void addToTab(Item item) {
        TAB_ITEMS.add(item);
    }

    /** A copy so the tab can loop safely. */
    public static List<Item> tabItems() {
        synchronized (TAB_ITEMS) {
            return List.copyOf(TAB_ITEMS);
        }
    }
}
