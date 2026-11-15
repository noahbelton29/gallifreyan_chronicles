package uk.nbforge.gallifreyan_chronicles.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
   The keybinds of the mod.
   The key mappings are built here but registered by each loader because Fabric and NeoForge register them and their category differently.
*/
public final class GCKeyMappings {
    private static @Nullable KeyMapping showControlNames;
    private static boolean controlNamesOn;

    private GCKeyMappings() {
    }

    public static KeyMapping createShowControlNames(KeyMapping.Category category) {
        showControlNames = new KeyMapping("key.gallifreyan_chronicles.show_control_names",
                InputConstants.KEY_N, category);
        return showControlNames;
    }

    public static boolean controlNamesOn() {
        return controlNamesOn;
    }

    public static void tick() {
        if (showControlNames == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        while (showControlNames.consumeClick()) {
            controlNamesOn = !controlNamesOn;
            if (minecraft.player != null) {
                minecraft.player.sendOverlayMessage(Component.translatable(controlNamesOn
                        ? "message.gallifreyan_chronicles.control_names_on"
                        : "message.gallifreyan_chronicles.control_names_off"));
            }
        }
    }
}
