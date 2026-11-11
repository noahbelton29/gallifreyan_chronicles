package com.noahtnt2009.gallifreyan_chronicles.client.gui.toast;

import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/* "trait discovered" popup. grows to fit the explanation */
public class DiscoveryToast implements Toast {
    private static final Identifier BACKGROUND = GCUtils.modIdentifier("toast/discovery");
    private static final String LANG = "discovery.gallifreyan_chronicles.";
    private static final int WIDTH = 200;
    private static final int TEXT_X = 30;
    private static final int TEXT_WIDTH = WIDTH - TEXT_X - 6;
    private static final int LINE_HEIGHT = 10;
    private static final int TOP = 7;
    private static final long BASE_MS = 6000L;
    private static final long MS_PER_LINE = 1500L;

    /* vanilla advancement toast colours. quirks use the challenge pink */
    private static final int HEADER_COLOUR = 0xFFFFFF00;
    private static final int QUIRK_COLOUR = 0xFFFF88FF;
    private static final int NAME_COLOUR = 0xFFFFFFFF;
    private static final int TEXT_COLOUR = 0xFFAAAAAA;

    private final ItemStack icon;
    private final Component header;
    private final int headerColour;
    private final List<FormattedCharSequence> titleLines;
    private final List<FormattedCharSequence> descriptionLines;
    private Visibility visibility = Visibility.SHOW;

    public DiscoveryToast(ItemStack icon, Component header, int headerColour, Component title, Component description) {
        this.icon = icon;
        this.header = header;
        this.headerColour = headerColour;
        Font font = Minecraft.getInstance().font;
        this.titleLines = font.split(title, TEXT_WIDTH);
        this.descriptionLines = font.split(description, TEXT_WIDTH);
    }

    public static void show(String kind, String id) {
        Component name = Component.translatable(LANG + kind + "." + id);
        Component header = Component.translatable(LANG + kind + ".found");
        Component description = Component.translatable(LANG + kind + "." + id + ".desc");
        if (kind.equals("personality")) {
            description = Component.translatable(LANG + "personality.intro").append(" ").append(description);
        }
        ItemStack icon = new ItemStack(kind.equals("trait") ? GCBlocks.TARDIS_CONSOLE : GCBlocks.TARDIS);
        int colour = kind.equals("disability") ? QUIRK_COLOUR : HEADER_COLOUR;
        Minecraft.getInstance().gui.toastManager().addToast(new DiscoveryToast(icon, header, colour, name, description));
    }

    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        long showFor = BASE_MS + descriptionLines.size() * MS_PER_LINE;
        visibility = fullyVisibleForMs >= showFor * manager.getNotificationDisplayTimeMultiplier()
                ? Visibility.HIDE
                : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, width(), height());
        graphics.fakeItem(icon, 8, 8);

        int y = TOP;
        graphics.text(font, header, TEXT_X, y, headerColour, false);
        y += LINE_HEIGHT + 1;
        for (FormattedCharSequence line : titleLines) {
            graphics.text(font, line, TEXT_X, y, NAME_COLOUR, false);
            y += LINE_HEIGHT;
        }
        y += 2;
        for (FormattedCharSequence line : descriptionLines) {
            graphics.text(font, line, TEXT_X, y, TEXT_COLOUR, false);
            y += LINE_HEIGHT;
        }
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        int lines = 1 + titleLines.size() + descriptionLines.size();
        return Math.max(SLOT_HEIGHT, TOP * 2 + 3 + lines * LINE_HEIGHT);
    }
}
