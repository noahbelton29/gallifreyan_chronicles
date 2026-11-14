package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;

import java.util.UUID;

public record TardisLinkComponent(UUID tardisId) implements Component {
}
