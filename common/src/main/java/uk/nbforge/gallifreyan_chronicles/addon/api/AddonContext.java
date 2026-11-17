package uk.nbforge.gallifreyan_chronicles.addon.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControl;

/**
 * What a java addon gets to work with.
 * It is a normal mod so mixins work too.
 * We call onInitialize while the game registers things, so the register methods work there.
 */
public interface AddonContext {
    String addonId();

    Logger logger();

    String modVersion();

    boolean isModLoaded(String modId);

    /** A folder just for this addon. */
    Path dataFolder();

    /** Reads config.json, empty if there is none yet. */
    JsonObject loadConfig();

    void saveConfig(JsonObject config);

    Identifier id(String path);

    /**
     * Added items show in the addons tab.
     * You still need the model, texture and lang.
     */
    <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory);

    /** Adds a block and its item. */
    Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory);

    /** Same but you build the item. */
    Block registerBlock(
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BiFunction<Block, Item.Properties, Item> itemFactory);

    Block registerBlockWithoutItem(String name, Function<BlockBehaviour.Properties, Block> factory);

    /** Put the sound itself in sounds.json. */
    SoundEvent registerSound(String name);

    /** Same with a max range. */
    SoundEvent registerSound(String name, float range);

    /** The id must start with the addon id and an underscore. */
    <T extends TardisControl> void registerControl(String id, MapCodec<T> codec, T control);

    void onTakeOff(Consumer<Tardis> listener);

    void onLand(Consumer<Tardis> listener);

    /** 20 ticks is 1 second. */
    void runLater(MinecraftServer server, int delayTicks, Runnable action);
}
