package com.noahtnt2009.gallifreyan_chronicles.handler;

import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaraniumLavaDamageHandler implements ServerLivingEntityEvents.AllowDamage {
    @Override
    public boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof Player player)) return true;

        var typeKey = source.typeHolder().unwrapKey();
        if (typeKey.isEmpty()) return true;
        var key = typeKey.get();
        if (!key.equals(DamageTypes.LAVA) &&
            !key.equals(DamageTypes.IN_FIRE) &&
            !key.equals(DamageTypes.ON_FIRE)) return true;

        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.is(GCItems.TARANIUM_HELMET) ||
                stack.is(GCItems.TARANIUM_CHESTPLATE) ||
                stack.is(GCItems.TARANIUM_LEGGINGS) ||
                stack.is(GCItems.TARANIUM_BOOTS)) {
                player.clearFire();
                return false;
            }
        }

        return true;
    }
}