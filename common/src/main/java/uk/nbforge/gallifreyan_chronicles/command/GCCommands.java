package uk.nbforge.gallifreyan_chronicles.command;

import static net.minecraft.commands.Commands.literal;
import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.tardisIdArgument;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

public final class GCCommands {
    private GCCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        return literal("gc")
                .then(TardisCommands.id())
                .then(tardis())
                .then(TardisCommands.debug().requires(GCCommands::isOp));
    }

    /**
     * Only server operators can use TARDIS commands.
     */
    private static boolean isOp(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tardis() {
        return literal("tardis")
                .then(TardisCommands.list().requires(GCCommands::isOp))
                .then(TardisCommands.deletion())
                .then(tardisIdArgument()
                        .requires(GCCommands::isOp)
                        .then(TardisCommands.info())
                        .then(TardisCommands.summon())
                        .then(TardisCommands.tp())
                        .then(TardisCommands.remove())
                        .then(TardisCommands.repair())
                        .then(ExteriorCommands.exterior())
                        .then(ConsoleCommands.console())
                        .then(MindCommands.mind())
                        .then(DataCommands.data()));
    }
}
