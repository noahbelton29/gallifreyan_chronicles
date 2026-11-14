package uk.nbforge.gallifreyan_chronicles.core.data;

public interface IdentifiableData<T extends IdentifiableData<T>> {
    String id();

    T withId(String id);
}
