package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class TaraniumLavaDamageHandler {
    @SubscribeEvent
    public void onLavaDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!event.getSource().is(DamageTypes.LAVA) &&
            !event.getSource().is(DamageTypes.IN_FIRE) &&
            !event.getSource().is(DamageTypes.ON_FIRE)) return;

        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.is(GCItems.TARANIUM_HELMET) ||
                stack.is(GCItems.TARANIUM_CHESTPLATE) ||
                stack.is(GCItems.TARANIUM_LEGGINGS) ||
                stack.is(GCItems.TARANIUM_BOOTS)) {
                event.setCanceled(true);
                player.clearFire();
                return;
            }
        }
    }
}