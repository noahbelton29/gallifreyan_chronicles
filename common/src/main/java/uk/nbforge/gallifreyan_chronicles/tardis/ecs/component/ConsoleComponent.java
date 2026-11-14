package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;

public record ConsoleComponent(String consoleId, String variantId) implements Component {
    public static ConsoleComponent ofDefault() {
        return of(TardisConsoleRegistry.getDefault());
    }

    public static ConsoleComponent of(TardisConsole console) {
        return new ConsoleComponent(console.id(), console.resolvedDefaultVariant());
    }

    public static ConsoleComponent of(TardisConsole console, String variantId) {
        String resolved = console.hasVariant(variantId) ? variantId : console.resolvedDefaultVariant();
        return new ConsoleComponent(console.id(), resolved);
    }

    public ConsoleComponent withVariant(String variantId) {
        TardisConsole console = console();
        String resolved = console.hasVariant(variantId) ? variantId : console.resolvedDefaultVariant();
        return new ConsoleComponent(consoleId, resolved);
    }

    public TardisConsole console() {
        return TardisConsoleRegistry.get(consoleId);
    }

    public String textureId() {
        return console().textureIdFor(variantId);
    }
}
