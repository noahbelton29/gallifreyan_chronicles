package com.noahtnt2009.gallifreyan_chronicles.addon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.addon.api.AddonContext;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlTypes;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/* what one addon gets. all its stuff is tagged with its id */
final class ModAddonContext implements AddonContext {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern NAME = Pattern.compile("[a-z0-9_./-]+");
    private static final Pattern CONTROL_ID = Pattern.compile("[a-z0-9_]+");
    private static final int MAX_LISTENER_FAILURES = 5;

    private final String addonId;
    private final Logger logger;
    private final String controlPrefix;

    ModAddonContext(String addonId) {
        this.addonId = addonId;
        this.logger = LoggerFactory.getLogger("GC Addon/" + addonId);
        this.controlPrefix = addonId.replace('.', '_').replace('-', '_') + "_";
    }

    @Override
    public String addonId() {
        return addonId;
    }

    @Override
    public Logger logger() {
        return logger;
    }

    @Override
    public String modVersion() {
        return AddonManager.modVersion();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return Services.PLATFORM.isModLoaded(modId);
    }

    @Override
    public Path dataFolder() {
        Path path = Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID).resolve("addons").resolve(addonId);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            logger.warn("Could not create {}", path, e);
        }
        return path;
    }

    @Override
    public JsonObject loadConfig() {
        Path file = dataFolder().resolve("config.json");
        if (!Files.exists(file)) return new JsonObject();
        try (Reader reader = Files.newBufferedReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            logger.warn("Could not read {}, using an empty config", file, e);
            return new JsonObject();
        }
    }

    @Override
    public void saveConfig(JsonObject config) {
        Path file = dataFolder().resolve("config.json");
        try {
            Files.writeString(file, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Could not save {}", file, e);
        }
    }

    @Override
    public Identifier id(String path) {
        if (!NAME.matcher(path).matches()) {
            throw new IllegalArgumentException("'" + path + "' may only use a-z, 0-9, _ . - and /");
        }
        return Identifier.fromNamespaceAndPath(addonId, path);
    }

    @Override
    public <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
        Identifier id = id(name);
        T item = Registry.register(BuiltInRegistries.ITEM, id,
                factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
        AddonContent.addToTab(item);
        return item;
    }

    @Override
    public Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory) {
        return registerBlock(name, factory, BlockItem::new);
    }

    @Override
    public Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory,
                               BiFunction<Block, Item.Properties, Item> itemFactory) {
        Identifier id = id(name);
        Block block = registerBlockWithoutItem(name, factory);
        Item item = Registry.register(BuiltInRegistries.ITEM, id,
                itemFactory.apply(block, new Item.Properties().useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM, id))));
        AddonContent.addToTab(item);
        return block;
    }

    @Override
    public Block registerBlockWithoutItem(String name, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = id(name);
        return Registry.register(BuiltInRegistries.BLOCK, id,
                factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id))));
    }

    @Override
    public SoundEvent registerSound(String name) {
        Identifier id = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    @Override
    public SoundEvent registerSound(String name, float range) {
        Identifier id = id(name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createFixedRangeEvent(id, range));
    }

    @Override
    public <T extends TardisControl> void registerControl(String id, MapCodec<T> codec, T control) {
        if (!CONTROL_ID.matcher(id).matches() || !id.startsWith(controlPrefix) || id.length() == controlPrefix.length()) {
            throw new IllegalArgumentException("Control id '" + id + "' must be lower case letters, digits and underscores, "
                    + "and start with '" + controlPrefix + "'");
        }
        if (TardisControlRegistry.isRegistered(id)) {
            throw new IllegalStateException("A control called '" + id + "' is already registered");
        }
        TardisControlTypes.register(id, codec);
        TardisControlRegistry.register(id, control);
    }

    @Override
    public void onTakeOff(Consumer<Tardis> listener) {
        TardisEvents.TAKE_OFF.register(guard("take off", listener, TardisEvents.TAKE_OFF));
    }

    @Override
    public void onLand(Consumer<Tardis> listener) {
        TardisEvents.LAND.register(guard("land", listener, TardisEvents.LAND));
    }

    @Override
    public void runLater(MinecraftServer server, int delayTicks, Runnable action) {
        GCScheduler.runLater(server, delayTicks, () -> {
            try {
                action.run();
            } catch (Throwable t) {
                if (t instanceof VirtualMachineError error) throw error;
                logger.error("A delayed task failed", t);
            }
        });
    }

    /* catches errors from the addon, drops the listener after too many */
    private Consumer<Tardis> guard(String eventName, Consumer<Tardis> listener, TardisEvents.Event event) {
        return new Consumer<>() {
            private int failures;

            @Override
            public void accept(Tardis tardis) {
                try {
                    listener.accept(tardis);
                } catch (Throwable t) {
                    if (t instanceof VirtualMachineError error) throw error;
                    logger.error("A {} listener failed", eventName, t);
                    if (++failures >= MAX_LISTENER_FAILURES) {
                        event.unregister(this);
                        logger.warn("Removed a {} listener after {} failures", eventName, MAX_LISTENER_FAILURES);
                    }
                }
            }
        };
    }
}
