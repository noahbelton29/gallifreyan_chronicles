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
 * its a normal mod so mixins work too. onInitialize gets called while the game is registering stuff so the register
 * methods work in there
 */
public interface AddonContext {
    String addonId();

    Logger logger();

    String modVersion();

    boolean isModLoaded(String modId);

    Path dataFolder();

    JsonObject loadConfig();

    void saveConfig(JsonObject config);

    Identifier id(String path);

    /** shows up in the addons tab, u still need the model, texture and lang */
    <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory);

    Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory);

    Block registerBlock(
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BiFunction<Block, Item.Properties, Item> itemFactory);

    Block registerBlockWithoutItem(String name, Function<BlockBehaviour.Properties, Block> factory);

    /** the sound itself still goes in sounds.json */
    SoundEvent registerSound(String name);

    SoundEvent registerSound(String name, float range);

    /** id has to start with the addon id and an underscore */
    <T extends TardisControl> void registerControl(String id, MapCodec<T> codec, T control);

    void onTakeOff(Consumer<Tardis> listener);

    void onLand(Consumer<Tardis> listener);

    void runLater(MinecraftServer server, int delayTicks, Runnable action);
}
