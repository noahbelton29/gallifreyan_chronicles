package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.core.item.MalletItem;
import uk.nbforge.gallifreyan_chronicles.core.item.PeaceOfferingItem;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.item.StattenheimRemoteItem;
import uk.nbforge.gallifreyan_chronicles.core.item.TardisKeyItem;
import uk.nbforge.gallifreyan_chronicles.core.item.MasterTardisKeyItem;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GCItems {

    public static final List<Item> ITEMS = new ArrayList<>();

    public static final Item TARANIUM_SCRAP = registerItem("taranium_scrap", Item::new);
    public static final Item TARANIUM_CRYSTAL = registerItem("taranium_crystal", Item::new);
    public static final Item REFINED_TARANIUM_CRYSTAL = registerItem("refined_taranium_crystal", Item::new);
    public static final Item TARANIUM_INGOT = registerItem("taranium_ingot", Item::new);

    public static final Item RAW_TIN = registerItem("raw_tin", Item::new);
    public static final Item TARDIS_KEY = registerItem("tardis_key", properties
            -> new TardisKeyItem(properties.fireResistant()));
    public static final Item MASTER_TARDIS_KEY = registerItem("master_tardis_key",
            properties -> new MasterTardisKeyItem(properties.component(
                    DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).fireResistant()));

    public static final Item MALLET = registerItem("mallet", properties -> new MalletItem(properties.stacksTo(1)));
    public static final Item JELLY_BABIES = registerItem("jelly_babies", properties -> new PeaceOfferingItem(properties.stacksTo(16)));
    public static final Item STATTENHEIM_REMOTE = registerItem("stattenheim_remote", properties -> new StattenheimRemoteItem(properties.stacksTo(1)));

    // TOOLS
    public static final Item TARANIUM_SWORD = registerItem("taranium_sword",
            properties -> new Item(properties.sword(GCToolMaterials.TARANIUM, 3, -2.4f).fireResistant()));

    public static final Item TARANIUM_PICKAXE = registerItem("taranium_pickaxe",
            properties -> new Item(properties.pickaxe(GCToolMaterials.TARANIUM, 1, -2.8f).fireResistant()));

    public static final Item TARANIUM_SHOVEL = registerItem("taranium_shovel",
            properties -> new Item(properties.shovel(GCToolMaterials.TARANIUM, 1.5f, -3.0f).fireResistant()));

    public static final Item TARANIUM_AXE = registerItem("taranium_axe",
            properties -> new Item(properties.axe(GCToolMaterials.TARANIUM, 6, -3.2f).fireResistant()));

    public static final Item TARANIUM_HOE = registerItem("taranium_hoe",
            properties -> new Item(properties.hoe(GCToolMaterials.TARANIUM, 0, -3f).fireResistant()));

    // ARMOR
    public static final Item TARANIUM_HELMET = registerItem("taranium_helmet",
            properties -> new Item(
                    properties.humanoidArmor(GCArmorMaterials.TARANIUM_ARMOR_MATERIAL, ArmorType.HELMET).fireResistant()
            ));

    public static final Item TARANIUM_CHESTPLATE = registerItem("taranium_chestplate",
            properties -> new Item(
                    properties.humanoidArmor(GCArmorMaterials.TARANIUM_ARMOR_MATERIAL, ArmorType.CHESTPLATE).fireResistant()
            ));

    public static final Item TARANIUM_LEGGINGS = registerItem("taranium_leggings",
            properties -> new Item(
                    properties.humanoidArmor(GCArmorMaterials.TARANIUM_ARMOR_MATERIAL, ArmorType.LEGGINGS).fireResistant()
            ));

    public static final Item TARANIUM_BOOTS = registerItem("taranium_boots",
            properties -> new Item(
                    properties.humanoidArmor(GCArmorMaterials.TARANIUM_ARMOR_MATERIAL, ArmorType.BOOTS).fireResistant()
            ));

    private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
        T item = Registry.register(
                BuiltInRegistries.ITEM,
                GCUtil.modIdentifier(name),
                factory.apply(new Item.Properties().setId(
                        ResourceKey.create(Registries.ITEM,
                                GCUtil.modIdentifier(name)
                        )
                ))
        );

        ITEMS.add(item);
        return item;
    }

    public static void registerItems() {
        Constants.LOG.info("Registered GC Items");
    }
}