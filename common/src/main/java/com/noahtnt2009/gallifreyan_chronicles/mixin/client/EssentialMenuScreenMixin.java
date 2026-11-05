package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.compat.essential.EssentialMenuCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class EssentialMenuScreenMixin {
    @Shadow
    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At("HEAD"))
    private void gc$tidyEssentialMenu(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                                      CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof PauseScreen) && !(self instanceof TitleScreen)) return;
        EssentialMenuCompat.sync(self, this::addRenderableWidget);
    }
}
