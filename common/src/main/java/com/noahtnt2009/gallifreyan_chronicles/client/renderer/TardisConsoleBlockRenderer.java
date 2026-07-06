package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisConsoleBlockModel;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class TardisConsoleBlockRenderer extends GeoBlockRenderer<@NotNull TardisConsoleBlockEntity, @NotNull TardisConsoleBlockRenderState> {
    public static final DataTicket<String> CONSOLE_ID = DataTicket.create("console_id", String.class);

    public TardisConsoleBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new TardisConsoleBlockModel());
    }

    @Override
    public @NotNull TardisConsoleBlockRenderState createRenderState() {
        return new TardisConsoleBlockRenderState();
    }

    @Override
    public void addRenderData(@NotNull TardisConsoleBlockEntity animatable, @Nullable Void relatedObject, @NotNull TardisConsoleBlockRenderState renderState, float partialTick) {
        renderState.addGeckolibData(CONSOLE_ID, animatable.getConsole().id());
    }
}