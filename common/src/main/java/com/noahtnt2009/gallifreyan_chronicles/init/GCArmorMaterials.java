package com.noahtnt2009.gallifreyan_chronicles.init;

import com.google.common.collect.Maps;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.Map;

public class GCArmorMaterials {
    static ResourceKey<? extends Registry<EquipmentAsset>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Identifier.parse("equipment_asset"));

    public static final ResourceKey<EquipmentAsset> TARANIUM_KEY =
            ResourceKey.create(REGISTRY_KEY,
                    GCUtils.of("taranium"));

    public static final ArmorMaterial TARANIUM_ARMOR_MATERIAL =
            new ArmorMaterial(
                    40,
                    makeDefense(4, 7, 9, 4, 20),
                    17,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    3.5F,
                    0.15F,
                    GCTags.Items.TARANIUM_REPAIRABLE,
                    TARANIUM_KEY
            );

    static Map<ArmorType, Integer> makeDefense(final int boots, final int legs, final int chest, final int helm, final int body) {
        return Maps.newEnumMap(Map.of(
                ArmorType.BOOTS, boots,
                ArmorType.LEGGINGS, legs,
                ArmorType.CHESTPLATE, chest,
                ArmorType.HELMET, helm,
                ArmorType.BODY, body
        ));
    }
}