package com.noahtnt2009.gallifreyan_chronicles.data;

public interface IdentifiableData<T extends IdentifiableData<T>> {
    String id();

    T withId(String id);
}
