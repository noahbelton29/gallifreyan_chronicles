package uk.nbforge.gallifreyan_chronicles.util;

import uk.nbforge.gallifreyan_chronicles.core.GCItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TaraniumArmorUtil {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private TaraniumArmorUtil() {
    }

    public static boolean blocksFireDamage(Player player, DamageSource source) {
        if (!isFireDamage(source) || !wearsAnyPiece(player)) return false;
        player.clearFire();
        return true;
    }

    private static boolean isFireDamage(DamageSource source) {
        return source.is(DamageTypes.LAVA) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE);
    }

    private static boolean wearsAnyPiece(Player player) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.is(GCItems.TARANIUM_HELMET) || stack.is(GCItems.TARANIUM_CHESTPLATE)
                    || stack.is(GCItems.TARANIUM_LEGGINGS) || stack.is(GCItems.TARANIUM_BOOTS)) {
                return true;
            }
        }
        return false;
    }
}
