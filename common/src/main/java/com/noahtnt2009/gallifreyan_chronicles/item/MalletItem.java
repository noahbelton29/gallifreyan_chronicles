package com.noahtnt2009.gallifreyan_chronicles.item;

import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisMallet;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.abuse.TardisTrauma;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class MalletItem extends Item {
    private static final int STRIKE_COOLDOWN_TICKS = 60;
    private static final int LOOK_TICKS = 20;

    public MalletItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        Optional<Tardis> tardis = tardisAt(level, context.getClickedPos());
        if (player == null || tardis.isEmpty()) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        TardisMallet.strike(tardis.get(), serverLevel, context.getClickedPos(), player);
        player.getCooldowns().addCooldown(context.getItemInHand(), STRIKE_COOLDOWN_TICKS);
        return InteractionResult.SUCCESS;
    }

    /* a scarred tardis notices you holding it */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND || !(owner instanceof Player player)) return;
        if (level.getGameTime() % LOOK_TICKS != 0) return;
        TardisTrauma.sawMallet(player, level);
    }

    static Optional<Tardis> tardisAt(Level level, BlockPos pos) {
        BlockPos exteriorPos = level.getBlockState(pos).getBlock() instanceof TardisTopBlock ? pos.below() : pos;
        if (level.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity exterior) {
            return TardisApi.of(exterior);
        }
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            return TardisApi.of(console);
        }
        return Optional.empty();
    }
}
