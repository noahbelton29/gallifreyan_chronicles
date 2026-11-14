package com.noahtnt2009.gallifreyan_chronicles.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;

public final class CommandUtil {
    private static final String KEY = "command.gallifreyan_chronicles.";
    private static final double LOOK_DISTANCE = 8.0;
    /* not called "id" so it doesn't clash with /gc tardis id */
    private static final String ID_ARG = "tardis_id";

    interface TardisAction {
        int run(Tardis tardis, String idText) throws CommandSyntaxException;
    }

    interface IdAction {
        int run(UUID id, String idText) throws CommandSyntaxException;
    }

    private CommandUtil() {
    }

    static int withTardisId(CommandContext<CommandSourceStack> ctx, IdAction action) throws CommandSyntaxException {
        String idText = StringArgumentType.getString(ctx, ID_ARG);
        UUID id;
        try {
            id = UUID.fromString(idText);
        } catch (IllegalArgumentException e) {
            return fail(ctx, "invalid_id", idText);
        }
        return action.run(id, idText);
    }

    static int withTardis(CommandContext<CommandSourceStack> ctx, TardisAction action) throws CommandSyntaxException {
        return withTardisId(ctx, (id, idText) -> {
            Optional<Tardis> tardis = TardisApi.get(ctx.getSource().getServer(), id);
            if (tardis.isEmpty()) return fail(ctx, "no_tardis_id", idText);
            return action.run(tardis.get(), idText);
        });
    }

    static Optional<UUID> idArgument(CommandContext<CommandSourceStack> ctx) {
        try {
            return Optional.of(UUID.fromString(StringArgumentType.getString(ctx, ID_ARG)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> tardisIdArgument() {
        return argument(ID_ARG, StringArgumentType.string()).suggests(CommandUtil::suggestTardisIds);
    }

    static CompletableFuture<Suggestions> suggestTardisIds(CommandContext<CommandSourceStack> ctx,
                                                           SuggestionsBuilder builder) {
        TardisManager.get(ctx.getSource().getServer()).getAllIds().forEach(id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }

    static TardisManager manager(CommandContext<CommandSourceStack> ctx) {
        return TardisManager.get(ctx.getSource().getServer());
    }

    static int fail(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
        ctx.getSource().sendFailure(Component.translatable(KEY + key, args));
        return 0;
    }

    static int success(CommandContext<CommandSourceStack> ctx, boolean tellOps, String key, Object... args) {
        Component message = Component.translatable(KEY + key, args);
        ctx.getSource().sendSuccess(() -> message, tellOps);
        return 1;
    }

    static void send(CommandContext<CommandSourceStack> ctx, Component message, boolean tellOps) {
        ctx.getSource().sendSuccess(() -> message, tellOps);
    }

    static MutableComponent text(String key, Object... args) {
        return Component.translatable(KEY + key, args);
    }

    static @Nullable ServerPlayer playerOrFail(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) fail(ctx, "player_only");
        return player;
    }

    static <T extends BlockEntity> Optional<T> findBlockEntity(CommandContext<CommandSourceStack> ctx,
                                                               @Nullable BlockPos pos, Class<T> type) {
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

    static Optional<BlockPos> lookedAtBlock(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return Optional.empty();

        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(LOOK_DISTANCE));
        ClipContext clip = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hit = ctx.getSource().getLevel().clip(clip);

        if (hit.getType() != HitResult.Type.BLOCK) return Optional.empty();
        return Optional.of(hit.getBlockPos());
    }

    static void syncConsolesTo(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            Services.NETWORK.sendTardisConsoleSync(player, TardisConsoleSyncPayload.create());
        }
    }

    static String describe(@Nullable BlockPos pos) {
        return pos == null ? "unplaced" : pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
