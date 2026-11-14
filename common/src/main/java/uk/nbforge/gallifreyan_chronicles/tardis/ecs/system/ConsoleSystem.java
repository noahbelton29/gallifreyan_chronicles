package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import uk.nbforge.gallifreyan_chronicles.ecs.Entity;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.ConsoleComponent;

public final class ConsoleSystem {
    private ConsoleSystem() {
    }

    public static TardisConsole consoleOf(Entity entity) {
        return entity.get(TardisComponentTypes.CONSOLE).console();
    }

    public static void setConsole(Entity entity, TardisConsole console) {
        entity.set(TardisComponentTypes.CONSOLE, ConsoleComponent.of(console));
    }

    public static void setConsole(Entity entity, TardisConsole console, String variantId) {
        entity.set(TardisComponentTypes.CONSOLE, ConsoleComponent.of(console, variantId));
    }

    public static String variantOf(Entity entity) {
        return entity.get(TardisComponentTypes.CONSOLE).variantId();
    }

    public static void setVariant(Entity entity, String variantId) {
        ConsoleComponent current = entity.get(TardisComponentTypes.CONSOLE);
        entity.set(TardisComponentTypes.CONSOLE, current.withVariant(variantId));
    }
}
