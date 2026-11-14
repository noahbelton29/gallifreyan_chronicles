package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.noahtnt2009.gallifreyan_chronicles.command.ConsoleCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.ControlCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.DataCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.DestinationCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.ExteriorCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.InteriorCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.MindCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.SubsystemCommands;
import com.noahtnt2009.gallifreyan_chronicles.command.TardisCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

import static com.noahtnt2009.gallifreyan_chronicles.command.CommandUtil.tardisIdArgument;
import static net.minecraft.commands.Commands.literal;

public final class GCCommands {
    private GCCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        return literal("gc")
                .then(tardis())
                .then(DataCommands.data().requires(GCCommands::isOp))
                .then(TardisCommands.debug().requires(GCCommands::isOp));
    }

    /* only server operators can use tardis commands */
    private static boolean isOp(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tardis() {
        return literal("tardis")
                /* anyone can get a tardis id */
                .then(TardisCommands.id())
                .then(TardisCommands.list().requires(GCCommands::isOp))
                .then(TardisCommands.deletion())
                .then(tardisIdArgument().requires(GCCommands::isOp)
                        .then(TardisCommands.info())
                        .then(TardisCommands.summon())
                        .then(TardisCommands.tp())
                        .then(TardisCommands.remove())
                        .then(TardisCommands.repair())
                        .then(ExteriorCommands.exterior())
                        .then(InteriorCommands.interior())
                        .then(ConsoleCommands.console())
                        .then(InteriorCommands.door())
                        .then(DestinationCommands.destination())
                        .then(ExteriorCommands.fade())
                        .then(ExteriorCommands.glow())
                        .then(ControlCommands.control())
                        .then(ControlCommands.controlsLegacy())
                        .then(SubsystemCommands.subsystem())
                        .then(MindCommands.mind()));
    }
}
