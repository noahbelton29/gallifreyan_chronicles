package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shared logic for "sync this whole registry to the client" payloads.
 * Each concrete payload record still declares its own Type/STREAM_CODEC
 * (Minecraft's CustomPacketPayload needs that per-record anyway), but
 * calls this helper instead of hand-writing clear+loop+log each time.
 */
public final class GCSyncPayload {
    private GCSyncPayload() {
    }

    public static <T extends IdentifiableData<T>> void apply(List<T> values, Runnable clear, Consumer<T> register, String label) {
        clear.run();
        values.forEach(register);
        Constants.LOG.info("Synced {} {}(s) to client", values.size(), label);
    }
}
