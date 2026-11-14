package uk.nbforge.gallifreyan_chronicles.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.tardisIdArgument;
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
