package uk.nbforge.gallifreyan_chronicles.tardis;

import java.util.Optional;
import java.util.UUID;

public interface TardisLinkable {
    Optional<UUID> getLinkedTardisId();

    void linkToTardis(UUID tardisId);

    void unlinkFromTardis();

    default boolean isLinked() {
        return getLinkedTardisId().isPresent();
    }
}
