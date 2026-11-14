package uk.nbforge.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static uk.nbforge.gallifreyan_chronicles.util.CommandUtil.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ConsoleCommands {
    private ConsoleCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> console() {
        return literal("console")
                .then(literal("get").executes(ConsoleCommands::get))
                .then(literal("set")
                        .then(argument("console_id", StringArgumentType.greedyString())
                                .suggests(ConsoleCommands::suggestConsoles)
                                .executes(ConsoleCommands::set)))
                .then(literal("link").executes(ConsoleCommands::link))
                .then(literal("unlink").executes(ConsoleCommands::unlink))
                .then(literal("variant")
                        .then(literal("set")
                                .then(argument("variant_id", StringArgumentType.greedyString())
                                        .suggests(ConsoleCommands::suggestVariants)
                                        .executes(ConsoleCommands::setVariant))));
    }

    private static CompletableFuture<Suggestions> suggestConsoles(CommandContext<CommandSourceStack> ctx,
                                                                  SuggestionsBuilder builder) {
        TardisConsoleRegistry.getAll().forEach(console -> builder.suggest(console.id()));
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestVariants(CommandContext<CommandSourceStack> ctx,
                                                                  SuggestionsBuilder builder) {
        idArgument(ctx).flatMap(id -> consoleOf(ctx, id))
                .map(TardisConsoleBlockEntity::getConsole)
                .ifPresent(console -> console.variantIds().forEach(builder::suggest));
        return builder.buildFuture();
    }

    static Optional<TardisConsoleBlockEntity> consoleOf(CommandContext<CommandSourceStack> ctx, UUID tardisId) {
        BlockPos pos = manager(ctx).getConsoleBlockPos(tardisId);
        return findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class);
    }

    private static int get(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            String consoleId = consoleOf(ctx, tardis.id()).map(be -> be.getConsole().id()).orElse("unknown");
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
            if (pos.isEmpty() || findBlockEntity(ctx, pos.get(), TardisConsoleBlockEntity.class).isEmpty()) {
                return fail(ctx, "not_looking_at_console");
            }
            ConsoleLinkSystem.link(ctx.getSource().getLevel(), tardis.id(), pos.get());
            return success(ctx, true, "link_console", idText);
        });
    }

    private static int unlink(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            manager(ctx).unlinkConsole(tardis.id());
            return success(ctx, true, "unlink_console", idText);
        });
    }
}
