package uk.nbforge.gallifreyan_chronicles.util;

import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;

public final class CommandUtil {
    private static final String KEY = "command.gallifreyan_chronicles.";
    private static final double LOOK_DISTANCE = 8.0;
    // Not called id so it does not clash with the gc tardis id command
    private static final String ID_ARG = "tardis_id";

    public interface TardisAction {
        int run(Tardis tardis, String idText) throws CommandSyntaxException;
    }

    public interface IdAction {
        int run(UUID id, String idText) throws CommandSyntaxException;
    }

    private CommandUtil() {}

    public static int withTardisId(CommandContext<CommandSourceStack> ctx, IdAction action)
            throws CommandSyntaxException {
        String idText = StringArgumentType.getString(ctx, ID_ARG);
        UUID id;
        try {
            id = UUID.fromString(idText);
        } catch (IllegalArgumentException e) {
            return fail(ctx, "invalid_id", idText);
        }
        return action.run(id, idText);
    }

    public static int withTardis(CommandContext<CommandSourceStack> ctx, TardisAction action)
            throws CommandSyntaxException {
        return withTardisId(ctx, (id, idText) -> {
            Optional<Tardis> tardis = TardisApi.get(ctx.getSource().getServer(), id);
            if (tardis.isEmpty()) return fail(ctx, "no_tardis_id", idText);
            return action.run(tardis.get(), idText);
        });
    }

    public static Optional<UUID> idArgument(CommandContext<CommandSourceStack> ctx) {
        try {
            return Optional.of(UUID.fromString(StringArgumentType.getString(ctx, ID_ARG)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> tardisIdArgument() {
        return argument(ID_ARG, StringArgumentType.string()).suggests(CommandUtil::suggestTardisIds);
    }

    public static CompletableFuture<Suggestions> suggestTardisIds(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        TardisManager.get(ctx.getSource().getServer()).getAllIds().forEach(id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }

    public static TardisManager manager(CommandContext<CommandSourceStack> ctx) {
        return TardisManager.get(ctx.getSource().getServer());
    }

    private static final Pattern UUID_SHAPE =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    /**
     * Every TARDIS id that reaches chat as a plain String argument to fail()/success()/text() is swapped for the
     * short, coloured, click-to-copy form - this is the one place that has to change for every command's output to
     * pick it up, instead of touching every call site in every command file.
     */
    private static Object[] colourIds(Object[] args) {
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String text && UUID_SHAPE.matcher(text).matches()) {
                try {
                    out[i] = idLink(UUID.fromString(text));
                    continue;
                } catch (IllegalArgumentException ignored) {
                    // Not actually a UUID, fall through and keep it as plain text.
                }
            }
            out[i] = args[i];
        }
        return out;
    }

    public static int fail(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
        ctx.getSource().sendFailure(Component.translatable(KEY + key, colourIds(args)));
        return 0;
    }

    public static int success(CommandContext<CommandSourceStack> ctx, boolean tellOps, String key, Object... args) {
        Component message = Component.translatable(KEY + key, colourIds(args));
        ctx.getSource().sendSuccess(() -> message, tellOps);
        return 1;
    }

    public static void send(CommandContext<CommandSourceStack> ctx, Component message, boolean tellOps) {
        ctx.getSource().sendSuccess(() -> message, tellOps);
    }

    /**
     * The first 8 characters of a TARDIS id, for compact display.
     */
    public static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    /**
     * The short id, coloured, underlined and click-to-copy/hover-to-reveal the full id, same convention as /gc id.
     */
    public static MutableComponent idLink(UUID id) {
        String full = id.toString();
        return Component.literal(shortId(id))
                .withStyle(style -> style.withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.CopyToClipboard(full))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to copy: " + full))));
    }

    /**
     * A {@code "=== <title>: <short id> ==="} header for a multi-line dump.
     */
    public static MutableComponent dataHeader(String title, UUID id) {
        return Component.literal("=== " + title + ": ")
                .withStyle(ChatFormatting.GOLD)
                .append(idLink(id))
                .append(Component.literal(" ===").withStyle(ChatFormatting.GOLD));
    }

    /**
     * One "  Field: value" line of a dump, with the field and value coloured apart so a long dump stays readable.
     * The field key can be written as "snake_case" or "space separated", either way it's shown as "Title Case".
     */
    public static MutableComponent dataLine(String field, String value) {
        return Component.literal("  ")
                .append(Component.literal(prettyLabel(field)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(value).withStyle(ChatFormatting.YELLOW));
    }

    private static String prettyLabel(String field) {
        StringBuilder out = new StringBuilder();
        for (String word : field.replace('_', ' ').split(" ")) {
            if (word.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    /**
     * Strips the "gallifreyan_chronicles:" namespace off a registry id, so a dump shows just "first_doctors_exterior".
     */
    public static String stripModId(String id) {
        String prefix = Constants.MOD_ID + ":";
        return id.startsWith(prefix) ? id.substring(prefix.length()) : id;
    }

    /**
     * "45.0" below a minute, "2m 30.0s" at or above one, so an absurdly large number of seconds stays readable.
     */
    public static String formatSeconds(double seconds) {
        if (seconds < 60.0) return String.valueOf(seconds);
        long whole = (long) seconds;
        long minutes = whole / 60;
        double remainder = seconds - minutes * 60.0;
        return minutes + "m " + remainder + "s";
    }

    /**
     * Sends a single dump line to the player only (never to other ops watching).
     */
    public static void sendLine(CommandContext<CommandSourceStack> ctx, Component line) {
        send(ctx, line, false);
    }

    public static MutableComponent text(String key, Object... args) {
        return Component.translatable(KEY + key, colourIds(args));
    }

    public static @Nullable ServerPlayer playerOrFail(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) fail(ctx, "player_only");
        return player;
    }

    public static <T extends BlockEntity> Optional<T> findBlockEntity(
            CommandContext<CommandSourceStack> ctx, @Nullable BlockPos pos, Class<T> type) {
        if (pos == null) return Optional.empty();

        ServerLevel here = ctx.getSource().getLevel();
        BlockEntity local = here.getBlockEntity(pos);
        if (type.isInstance(local)) return Optional.of(type.cast(local));

        for (ServerLevel level : ctx.getSource().getServer().getAllLevels()) {
            if (level == here) continue;
            BlockEntity other = level.getBlockEntity(pos);
            if (type.isInstance(other)) return Optional.of(type.cast(other));
        }
        return Optional.empty();
    }

    public static Optional<BlockPos> lookedAtBlock(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return Optional.empty();

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LOOK_DISTANCE));
        ClipContext clip = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hit = ctx.getSource().getLevel().clip(clip);

        if (hit.getType() != HitResult.Type.BLOCK) return Optional.empty();
        return Optional.of(hit.getBlockPos());
    }

    public static void syncConsolesTo(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            Services.NETWORK.sendTardisConsoleSync(player, TardisConsoleSyncPayload.create());
        }
    }

    public static String describe(@Nullable BlockPos pos) {
        return pos == null ? "unplaced" : pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
