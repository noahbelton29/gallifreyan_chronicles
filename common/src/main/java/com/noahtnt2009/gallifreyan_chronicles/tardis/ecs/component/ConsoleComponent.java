package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;

public record ConsoleComponent(String consoleId) implements Component {
    public static final Codec<ConsoleComponent> CODEC =
            Codec.STRING.xmap(ConsoleComponent::new, ConsoleComponent::consoleId);

    public static ConsoleComponent ofDefault() {
        return new ConsoleComponent(TardisConsoleRegistry.getDefault().id());
    }

    public static ConsoleComponent of(TardisConsole console) {
        return new ConsoleComponent(console.id());
    }

    public TardisConsole console() {
        return TardisConsoleRegistry.get(consoleId);
    }
}
