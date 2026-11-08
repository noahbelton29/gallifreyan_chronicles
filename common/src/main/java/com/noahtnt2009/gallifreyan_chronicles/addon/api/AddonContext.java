package com.noahtnt2009.gallifreyan_chronicles.addon.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/* What a java addon gets to work with.
   It's a normal mod, so mixins work too.
   We call onInitialize while the game registers stuff,
   so the register methods work there. */
public interface AddonContext {
    /* the addon's id */
    String addonId();

    /* logger with the addon's name */
    Logger logger();

    /* our mod's version */
    String modVersion();

    /* is another mod loaded? */
    boolean isModLoaded(String modId);

    /* a folder just for this addon */
    Path dataFolder();

    /* read config.json, empty if none yet */
    JsonObject loadConfig();

    /* write config.json */
    void saveConfig(JsonObject config);

    /* an id in the addon's namespace */
    Identifier id(String path);

    /* add an item, it shows in the addons tab.
       You still need the model, texture and lang. */
    <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory);

    /* add a block and its item */
    Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory);

    /* same, but you build the item */
    Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory,
                        BiFunction<Block, Item.Properties, Item> itemFactory);

    /* a block with no item */
    Block registerBlockWithoutItem(String name, Function<BlockBehaviour.Properties, Block> factory);

    /* add a sound event (put the sound in sounds.json) */
    SoundEvent registerSound(String name);

    /* same, with a max range */
    SoundEvent registerSound(String name, float range);

    /* add a console control.
       id must start with the addon id and _ */
    <T extends TardisControl> void registerControl(String id, MapCodec<T> codec, T control);

    /* runs when a tardis takes off */
    void onTakeOff(Consumer<Tardis> listener);

    /* runs when a tardis lands */
    void onLand(Consumer<Tardis> listener);

    /* run something later (20 ticks = 1 second) */
    void runLater(MinecraftServer server, int delayTicks, Runnable action);
}
