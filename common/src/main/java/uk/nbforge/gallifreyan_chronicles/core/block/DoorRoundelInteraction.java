package uk.nbforge.gallifreyan_chronicles.core.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;

/**
 * Shift + right click on an interior door with one of this mod's roundel blocks gives the door that block's look.
 * Shift + right click with a brush takes it off again.
 *
 * <p>Sneaking with an item in hand makes vanilla skip the block's own use method and try to place the item instead,
 * so this can't live on the block. Each loader calls it from its right-click-block event.</p>
 */
public final class DoorRoundelInteraction {
    private DoorRoundelInteraction() {}

    public static InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof InteriorDoorBlock)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        boolean brush = stack.is(Items.BRUSH);
        Identifier roundel = roundelIdOf(stack);
        if (!brush && roundel == null) return InteractionResult.PASS;

        if (!(level.getBlockEntity(InteriorDoorBlock.masterBlockPos(state, pos))
                instanceof InteriorDoorBlockEntity door)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!door.canTakeRoundel()) {
            player.sendOverlayMessage(Component.translatable("overlay.gallifreyan_chronicles.door_style_primary")
                    .withStyle(ChatFormatting.RED));
        } else if (brush) {
            door.setRoundelBlock("");
            player.sendOverlayMessage(Component.translatable("overlay.gallifreyan_chronicles.door_style_cleared"));
        } else if (!hasWallBlock(roundel)) {
            player.sendOverlayMessage(
                    Component.translatable("overlay.gallifreyan_chronicles.door_style_no_match", stack.getHoverName())
                            .withStyle(ChatFormatting.RED));
        } else {
            door.setRoundelBlock(roundel.toString());
            player.sendOverlayMessage(
                    Component.translatable("overlay.gallifreyan_chronicles.door_style_set", stack.getHoverName()));
        }
        return InteractionResult.SUCCESS;
    }

    /** true if the roundel block has a plain wall block to take the door's sides from */
    public static boolean hasWallBlock(Identifier roundelBlock) {
        for (Identifier candidate : wallBlockCandidates(roundelBlock)) {
            if (BuiltInRegistries.BLOCK.containsKey(candidate)) return true;
        }
        return false;
    }

    /** the ids of the plain blocks a roundel block could be the roundel version of, best match first. the block and
     * its texture share a name, so the client uses these for the texture too */
    public static List<Identifier> wallBlockCandidates(Identifier roundelBlock) {
        String path = roundelBlock.getPath();
        String namespace = roundelBlock.getNamespace();
        List<Identifier> candidates = new ArrayList<>();

        String plain = path.replace("_half", "").replace("_hexagonal", "");
        if (plain.contains("_roundel")) {
            candidates.add(Identifier.fromNamespaceAndPath(namespace, plain.replace("_roundel", "_wall")));
            candidates.add(Identifier.fromNamespaceAndPath(namespace, plain.replaceAll("_roundel(_alt)?", "_wall")));
            candidates.add(Identifier.fromNamespaceAndPath(
                    namespace,
                    plain.replaceAll("_roundel(_alt)?", "_wall").replaceAll("_wall_block_\\d+", "_wall_block")));
        }

        int cut = path.indexOf("_half_roundel");
        if (cut < 0) cut = path.indexOf("_roundel");
        if (cut > 0) {
            String prefix = path.substring(0, cut);
            candidates.add(Identifier.fromNamespaceAndPath(namespace, prefix + "_planks"));
            candidates.add(Identifier.withDefaultNamespace(prefix + "_planks"));
            candidates.add(Identifier.fromNamespaceAndPath(namespace, prefix));
            candidates.add(Identifier.withDefaultNamespace(prefix));
        }
        return candidates;
    }

    /* the id of this mod's roundel block if the item is one, otherwise null */
    private static @Nullable Identifier roundelIdOf(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return null;
        Identifier id = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        return id.getNamespace().equals(Constants.MOD_ID) && id.getPath().contains("roundel") ? id : null;
    }
}
