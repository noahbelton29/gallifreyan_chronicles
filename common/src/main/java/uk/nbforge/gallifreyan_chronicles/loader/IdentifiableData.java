package uk.nbforge.gallifreyan_chronicles.loader;

public interface IdentifiableData<T extends IdentifiableData<T>> {
    String id();

    T withId(String id);
}
