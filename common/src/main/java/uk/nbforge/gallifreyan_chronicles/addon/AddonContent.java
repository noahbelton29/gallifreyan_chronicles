package uk.nbforge.gallifreyan_chronicles.addon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.Item;

public final class AddonContent {
    private static final List<Item> TAB_ITEMS = Collections.synchronizedList(new ArrayList<>());

    private AddonContent() {}

    public static void addToTab(Item item) {
        TAB_ITEMS.add(item);
    }

    public static List<Item> tabItems() {
        synchronized (TAB_ITEMS) {
            return List.copyOf(TAB_ITEMS);
        }
    }
}
