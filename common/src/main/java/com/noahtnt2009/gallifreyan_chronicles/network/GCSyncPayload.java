package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;

import com.noahtnt2009.gallifreyan_chronicles.client.GCClientHooks;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public final class GCSyncPayload {
    private GCSyncPayload() {
    }

    public static <T extends IdentifiableData<T>> void apply(List<T> values, Runnable clear, Consumer<T> register, String label) {
        clear.run();
        values.forEach(register);
        Constants.LOG.info("Synced {} {}(s) to client", values.size(), label);
    }

    public static <T extends IdentifiableData<T>> Set<String> idsOf(Collection<T> values) {
        return values.stream().map(IdentifiableData::id).collect(Collectors.toSet());
    }

    public static <T extends IdentifiableData<T>> void reloadIfChanged(Set<String> before, Collection<T> after) {
        if (!idsOf(after).equals(before)) {
            GCClientHooks.reloadResourcePacks();
        }
    }
}
