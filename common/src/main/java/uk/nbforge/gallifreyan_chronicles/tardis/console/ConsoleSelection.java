package uk.nbforge.gallifreyan_chronicles.tardis.console;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisConsoleRegistry;

public record ConsoleSelection(String consoleId, String variantId) {
    public static final Codec<ConsoleSelection> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.fieldOf("console_id").forGetter(ConsoleSelection::consoleId),
                    Codec.STRING.optionalFieldOf("variant_id", "").forGetter(ConsoleSelection::variantId))
            .apply(inst, ConsoleSelection::new));

    public static ConsoleSelection ofDefault() {
        return of(TardisConsoleRegistry.getDefault());
    }

    public static ConsoleSelection of(TardisConsole console) {
        return new ConsoleSelection(console.id(), console.resolvedDefaultVariant());
    }

    /** unknown variant falls back to the console's default one */
    public static ConsoleSelection of(TardisConsole console, String variantId) {
        String resolved = console.hasVariant(variantId) ? variantId : console.resolvedDefaultVariant();
        return new ConsoleSelection(console.id(), resolved);
    }

    /** keeps the saved console id even if that console isnt registered anymore */
    public ConsoleSelection withVariant(String variantId) {
        TardisConsole console = console();
        String resolved = console.hasVariant(variantId) ? variantId : console.resolvedDefaultVariant();
        return new ConsoleSelection(consoleId, resolved);
    }

    public TardisConsole console() {
        return TardisConsoleRegistry.get(consoleId);
    }
}
