package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import java.util.UUID;
import uk.nbforge.gallifreyan_chronicles.ecs.Component;

public record TardisLinkComponent(UUID tardisId) implements Component {}
