package uk.nbforge.gallifreyan_chronicles.command;

import static net.minecraft.commands.Commands.literal;
import static uk.nbforge.gallifreyan_chronicles.command.CommandUtil.*;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisTopBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionManager;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.deletion.TardisDeletion;
import uk.nbforge.gallifreyan_chronicles.tardis.deletion.TardisDeletionMode;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisLanding;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisDamage;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInteriors;

public final class TardisCommands {
    private static final int SUMMON_DISTANCE = 5;

    private TardisCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> list() {
        return literal("list").executes(TardisCommands::list);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> deletion() {
        return literal("deletion")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(literal("on").executes(ctx -> setDeletionMode(ctx, true)))
                .then(literal("off").executes(ctx -> setDeletionMode(ctx, false)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> info() {
        return literal("info").executes(TardisCommands::info);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> summon() {
        return literal("summon").executes(TardisCommands::summon);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> repair() {
        return literal("repair").executes(TardisCommands::repair);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> tp() {
        return literal("tp")
                .then(literal("exterior").executes(TardisCommands::tpExterior))
                .then(literal("interior").executes(TardisCommands::tpInterior));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> id() {
        return literal("id").executes(TardisCommands::id);
    }

    private static int tpExterior(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = playerOrFail(ctx);
            if (player == null) return 0;
            if (tardis.exteriorPos().isEmpty()) return fail(ctx, "no_exterior", idText);

            TardisInteriors.exitInterior(tardis, player);
            return success(ctx, false, "tp_exterior", idText);
        });
    }

    private static int tpInterior(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = playerOrFail(ctx);
            if (player == null) return 0;

            TardisDimensionManager.getOrCreate(tardis);
            TardisDimensionManager.requestEntry(tardis, player);
            return success(ctx, false, "tp_interior", idText);
        });
    }

    private static int id(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = playerOrFail(ctx);
        if (player == null) return 0;

        ServerLevel level = ctx.getSource().getLevel();
        Optional<Tardis> tardis =
                lookedAtBlock(ctx).flatMap(pos -> tardisAt(level, pos)).or(() -> Tardis.ofLevel(level));
        if (tardis.isEmpty()) return fail(ctx, "no_tardis_here");

        send(ctx, text("tardis_id", idLink(tardis.get().id())), false);
        return 1;
    }

    /** top half of the exterior counts as the exterior too */
    private static Optional<Tardis> tardisAt(ServerLevel level, BlockPos pos) {
        BlockPos exteriorPos = level.getBlockState(pos).getBlock() instanceof TardisTopBlock ? pos.below() : pos;
        if (level.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity exterior) return Tardis.of(exterior);
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) return Tardis.of(console);
        return Optional.empty();
    }

    private static int repair(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            if (!TardisDamage.isCrashed(tardis)) return fail(ctx, "not_crashed", idText);
            TardisDamage.repair(tardis);
            return success(ctx, true, "repaired", idText);
        });
    }

    public static LiteralArgumentBuilder<CommandSourceStack> remove() {
        return literal("remove").executes(TardisCommands::remove);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> debug() {
        return literal("debug")
                .then(literal("exteriors").executes(TardisCommands::listExteriors))
                .then(literal("controls").executes(TardisCommands::listControls))
                .then(literal("tardis").then(tardisIdArgument().executes(TardisCommands::info)));
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        Collection<TardisData> tardises = manager(ctx).getAllTardises();
        if (tardises.isEmpty()) {
            success(ctx, false, "no_tardises");
            return 0;
        }

        for (TardisData data : tardises) {
            BlockPos pos = data.resolveExteriorBlockPos();
            success(ctx, false, "list_tardis", data.id().toString(), exteriorIdAt(ctx, pos), describe(pos));
        }
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            BlockPos pos = tardis.data().resolveExteriorBlockPos();
            String owner = tardis.ownerId() != null ? tardis.ownerId().toString() : "None";

            success(ctx, false, "debug", idText);
            success(ctx, false, "owner", owner);
            success(ctx, false, "exterior", exteriorIdAt(ctx, pos));
            return success(ctx, false, "position", describe(pos));
        });
    }

    private static String exteriorIdAt(CommandContext<CommandSourceStack> ctx, BlockPos pos) {
        return findBlockEntity(ctx, pos, TardisExteriorBlockEntity.class)
                .map(be -> be.getExterior().id())
                .orElse("unknown");
    }

    private static int setDeletionMode(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        ServerPlayer player = playerOrFail(ctx);
        if (player == null) return 0;

        TardisDeletionMode.setEnabled(player.getUUID(), enabled);
        String key = enabled ? "deletion_mode_on" : "deletion_mode_off";
        send(ctx, text(key).withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GREEN), false);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardisId(ctx, (id, idText) -> {
            Tardis tardis = Tardis.get(ctx.getSource().getServer(), id).orElse(null);
            if (tardis == null) return fail(ctx, "no_tardis_id", idText);
            TardisDeletion.removeCompletely(tardis);
            return success(ctx, true, "remove_tardis", idText);
        });
    }

    private static int summon(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return withTardis(ctx, (tardis, idText) -> {
            ServerPlayer player = playerOrFail(ctx);
            if (player == null) return 0;
            return moveInFrontOf(ctx, tardis, player, idText);
        });
    }

    private static int moveInFrontOf(
            CommandContext<CommandSourceStack> ctx, Tardis tardis, ServerPlayer player, String idText) {
        ServerLevel oldLevel = tardis.exteriorLevel().orElse(null);
        BlockPos oldPos = tardis.exteriorPos().orElse(null);
        if (oldLevel == null || oldPos == null || tardis.exterior().isEmpty()) return fail(ctx, "no_exterior", idText);

        ServerLevel level = ctx.getSource().getLevel();
        if (TardisDimensionUtil.isInterior(level)) return fail(ctx, "summon_inside_tardis");
        BlockPos pos = player.blockPosition().relative(player.getDirection(), SUMMON_DISTANCE);
        boolean samePlace = level == oldLevel && pos.equals(oldPos);
        if (!samePlace && !level.getBlockState(pos).canBeReplaced()) return fail(ctx, "summon_blocked");

        if (TardisLanding.moveExterior(tardis, level, pos) == null) return fail(ctx, "no_exterior", idText);
        return success(ctx, true, "summon_tardis", idText, pos.toShortString());
    }

    private static int listExteriors(CommandContext<CommandSourceStack> ctx) {
        String ids =
                TardisExteriorRegistry.getAll().stream().map(TardisExterior::id).collect(Collectors.joining(", "));
        return success(ctx, false, "list_exterior", ids);
    }

    private static int listControls(CommandContext<CommandSourceStack> ctx) {
        Optional<TardisConsoleBlockEntity> console =
                lookedAtBlock(ctx).flatMap(pos -> findBlockEntity(ctx, pos, TardisConsoleBlockEntity.class));
        if (console.isEmpty()) return fail(ctx, "not_looking_at_console");

        List<TardisControlEntity> controls = console.get().getControlEntities();
        if (controls.isEmpty()) {
            success(ctx, false, "no_control_entities");
            return 0;
        }

        ServerPlayer player = playerOrFail(ctx);
        if (player == null) return 0;

        controls.forEach(control -> control.printPosition(player));
        return controls.size();
    }
}
