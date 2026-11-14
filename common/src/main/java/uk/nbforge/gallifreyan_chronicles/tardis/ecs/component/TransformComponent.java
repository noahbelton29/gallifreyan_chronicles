package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;

public record TransformComponent(float yaw) implements Component {
    public static final TransformComponent IDENTITY = new TransformComponent(0f);
}
