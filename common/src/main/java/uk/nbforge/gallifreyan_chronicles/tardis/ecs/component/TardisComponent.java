package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;


import java.util.UUID;

public record TardisComponent(UUID tardisId, UUID ownerId, String ownerName) {
}
