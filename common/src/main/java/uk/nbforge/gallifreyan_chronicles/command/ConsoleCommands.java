package uk.nbforge.gallifreyan_chronicles.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.*;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;

public final class ConsoleCommands {
    private ConsoleCommands() {}

    /**
     * Linking and unlinking a console block to a TARDIS. Getting/setting which console and its variant live under "data" now.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> console() {
        return literal("console")
                .then(literal("link").executes(ConsoleCommands::link))
                .then(literal("unlink").executes(ConsoleCommands::unlink));
    }

    /**
     * {@code data console [<console_id>] [variant [<variant_id>]]}
     */
    public static LiteralArgumentBuilder<CommandSourceStack> consoleData() {
        return literal("console")
                .executes(ConsoleCommands::get)
                .then(argument("console_id", StringArgumentType.greedyString())
                        .suggests(ConsoleCommands::suggestConsoles)
                        .executes(ConsoleCommands::set))
                .then(literal("variant")
                        .executes(ConsoleCommands::getVariant)
                        .then(argument("variant_id", StringArgumentType.greedyString())
                                .suggests(ConsoleCommands::suggestVariants)
                                .executes(ConsoleCommands::setVariant)));
    }

    private static CompletableFuture<Suggestions> suggestConsoles(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        TardisConsoleRegistry.getAll().forEach(console -> builder.suggest(console.id()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestVariants(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        idArgument(ctx)
                .flatMap(id -> consoleOf(ctx, id))
                .map(TardisConsoleBlockEntity::getConsole)
                .ifPresent(console -> console.variantIds().forEach(builder::suggest));
        return builder.buildFuture();
    }

    static Optional<TardisConsoleBlockEntity> consoleOf(CommandContext<CommandSourceStack> ctx, UUID tardisId) {
        TardisData data = manager(ctx).getTardis(tardisId);
        BlockPos pos = data == null ? null : data.getConsoleBlockPos();
        return findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class);
    }

    static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            String consoleId =
                    consoleOf(ctx, tardis.id()).map(be -> be.getConsole().id()).orElse("unknown");
            return success(ctx, false, "get_console", idText, consoleId);
        });
    }

    private static int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String consoleId = StringArgumentType.getString(ctx, "console_id");
        return withTardis(ctx, (tardis, idText) -> {
            if (!TardisConsoleRegistry.contains(consoleId)) return fail(ctx, "unknown_console", consoleId);

            Optional<TardisConsoleBlockEntity> be = consoleOf(ctx, tardis.id());
            if (be.isEmpty()) return fail(ctx, "no_console", idText);

            be.get().setConsole(TardisConsoleRegistry.get(consoleId));
            ConsoleLinkSystem.spawnDefaultControls(be.get());
            syncConsolesTo(ctx);
            return success(ctx, true, "set_console", idText, consoleId);
        });
    }

    static int getVariant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisConsoleBlockEntity> be = consoleOf(ctx, tardis.id());
            return be.map(tardisConsoleBlockEntity ->
                            success(ctx, false, "get_console_variant", idText, tardisConsoleBlockEntity.getVariant()))
                    .orElseGet(() -> fail(ctx, "no_console", idText));
        });
    }

    private static int setVariant(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String variantId = StringArgumentType.getString(ctx, "variant_id");
        return withTardis(ctx, (tardis, idText) -> {
            Optional<TardisConsoleBlockEntity> be = consoleOf(ctx, tardis.id());
            if (be.isEmpty()) return fail(ctx, "no_console", idText);

            TardisConsole console = be.get().getConsole();
            if (!console.hasVariant(variantId)) return fail(ctx, "unknown_console_variant", variantId, console.id());

            be.get().setVariant(variantId);
            syncConsolesTo(ctx);
            return success(ctx, true, "set_console_variant", idText, variantId);
        });
    }

    private static int link(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            Optional<BlockPos> pos = lookedAtBlock(ctx);
            if (pos.isEmpty()
                    || findBlockEntity(ctx, pos.get(), TardisConsoleBlockEntity.class)
                            .isEmpty()) {
                return fail(ctx, "not_looking_at_console");
            }
            ConsoleLinkSystem.link(ctx.getSource().getLevel(), tardis.id(), pos.get());
            return success(ctx, true, "link_console", idText);
        });
    }

    private static int unlink(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            tardis.data().setConsoleBlockPos(null);
            return success(ctx, true, "unlink_console", idText);
        });
    }
}
