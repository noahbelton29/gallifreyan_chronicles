package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLinkSystem;

import java.util.Optional;
import java.util.UUID;

public interface TardisLinkable {
    Entity asEntity();

    default Optional<UUID> getLinkedTardisId() {
        return TardisLinkSystem.linkedTardisId(asEntity());
    }

    default void linkToTardis(UUID tardisId) {
        TardisLinkSystem.link(asEntity(), tardisId);
    }

    default void unlinkFromTardis() {
        TardisLinkSystem.unlink(asEntity());
    }

    default boolean isLinked() {
        return getLinkedTardisId().isPresent();
    }

}
