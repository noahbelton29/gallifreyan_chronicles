package uk.nbforge.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisControlRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/** Whether a console control is on or off lives entirely under "data" now, since it goes through the control's real activate() logic. */
public final class ControlCommands {
    private ControlCommands() {
    }

    /** data control <control_id> [on|off] */
    public static LiteralArgumentBuilder<CommandSourceStack> controlData() {
        return literal("control")
                .then(argument("control", StringArgumentType.word())
                        .suggests(ControlCommands::suggestControls)
                        .executes(ControlCommands::get)
                        .then(literal("on").executes(ctx -> set(ctx, true)))
                        .then(literal("off").executes(ctx -> set(ctx, false))));
    }

    private static CompletableFuture<Suggestions> suggestControls(CommandContext<CommandSourceStack> ctx,
                                                                  SuggestionsBuilder builder) {
        Optional<TardisConsoleBlockEntity> console = idArgument(ctx).flatMap(id -> ConsoleCommands.consoleOf(ctx, id));
        if (console.isPresent()) {
            console.get().getControlEntities().forEach(control -> builder.suggest(control.getControlId()));
        } else {
            TardisControlRegistry.allIds().forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String controlId = StringArgumentType.getString(ctx, "control");
        return withTardis(ctx, (tardis, idText) -> {
            if (!TardisControlRegistry.allIds().contains(controlId)) return fail(ctx, "unknown_control", controlId);

            Optional<TardisConsoleBlockEntity> console = tardis.console();
            if (console.isEmpty()) return fail(ctx, "no_console", idText);
            if (findControl(console.get(), controlId) == null) return fail(ctx, "control_not_on_console", controlId, idText);

            boolean on = console.get().getControlState(controlId);
            return success(ctx, false, "get_control", controlId, idText, on ? "on" : "off");
        });
    }

    private static int set(CommandContext<CommandSourceStack> ctx, boolean on) throws CommandSyntaxException {
        String controlId = StringArgumentType.getString(ctx, "control");
        String state = on ? "on" : "off";

        return withTardis(ctx, (tardis, idText) -> {
            if (!TardisControlRegistry.allIds().contains(controlId)) return fail(ctx, "unknown_control", controlId);

            Optional<TardisConsoleBlockEntity> console = tardis.console();
            if (console.isEmpty()) return fail(ctx, "no_console", idText);

            TardisControlEntity control = findControl(console.get(), controlId);
            if (control == null) return fail(ctx, "control_not_on_console", controlId, idText);
            if (console.get().getControlState(controlId) == on) return fail(ctx, "control_already", controlId, state);

            InteractionResult result = control.activate(ctx.getSource().getPlayer(), InteractionHand.MAIN_HAND);
            if (result != InteractionResult.SUCCESS) return fail(ctx, "control_refused", controlId, state);

            return success(ctx, true, "set_control", controlId, idText, state);
        });
    }

    private static TardisControlEntity findControl(TardisConsoleBlockEntity console, String controlId) {
        for (TardisControlEntity control : console.getControlEntities()) {
            if (control.getControlId().equals(controlId)) return control;
        }
        return null;
    }
}
