package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.geckolib.GeckoLibConstants;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import java.util.Map;

public class TardisConsoleBlockRenderState extends BlockEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> data = new Reference2ObjectOpenHashMap<>();

    @Override
    public <D> void addGeckolibData(@NonNull DataTicket<D> ticket, @Nullable D value) {
        data.put(ticket, value);
    }

    @Override
    public boolean hasGeckolibData(@NonNull DataTicket<?> ticket) {
        return data.containsKey(ticket);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <D> D getGeckolibData(@NonNull DataTicket<D> ticket) {
        Object value = data.get(ticket);
        if (value == null && !hasGeckolibData(ticket))
            throw new IllegalArgumentException("DataTicket not found: " + ticket);
        try {
            return (D) value;
        } catch (ClassCastException ex) {
            GeckoLibConstants.LOGGER.error("Wrong type for DataTicket {}", ticket, ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <D> D getOrDefaultGeckolibData(@NonNull DataTicket<D> ticket, @Nullable D defaultValue) {
        Object value = data.get(ticket);
        if (value == null && !hasGeckolibData(ticket)) return defaultValue;
        try {
            return (D) value;
        } catch (ClassCastException ex) {
            GeckoLibConstants.LOGGER.error("Wrong type for DataTicket {}", ticket, ex);
            return defaultValue;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @ApiStatus.Internal
    @Override
    public @NonNull Map<DataTicket<?>, Object> getDataMap() {
        return data;
    }
}