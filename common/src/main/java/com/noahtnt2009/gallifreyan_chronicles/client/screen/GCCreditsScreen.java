package com.noahtnt2009.gallifreyan_chronicles.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GCCreditsScreen extends Screen {
    private final Screen parent;

    public GCCreditsScreen(Screen parent) {
        super(Component.translatable("menu.gallifreyan_chronicles.credits"));
        this.parent = parent;
    }

    @Override
    protected void init() {

    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }
}