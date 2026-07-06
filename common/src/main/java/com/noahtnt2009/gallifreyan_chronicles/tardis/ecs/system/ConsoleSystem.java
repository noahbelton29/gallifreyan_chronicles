package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.ConsoleComponent;

public final class ConsoleSystem {
    private ConsoleSystem() {
    }

    public static TardisConsole consoleOf(Entity entity) {
        return entity.get(TardisComponentTypes.CONSOLE).console();
    }

    public static void setConsole(Entity entity, TardisConsole console) {
        entity.set(TardisComponentTypes.CONSOLE, ConsoleComponent.of(console));
    }
}
