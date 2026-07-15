package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;

public record ConsoleComponent(String consoleId, String variantId) implements Component {
    public static final Codec<ConsoleComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("console_id").forGetter(ConsoleComponent::consoleId),
            Codec.STRING.optionalFieldOf("variant_id", "").forGetter(ConsoleComponent::variantId)
    ).apply(inst, ConsoleComponent::new));

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
