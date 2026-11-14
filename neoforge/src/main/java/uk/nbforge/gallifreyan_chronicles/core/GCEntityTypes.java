package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisKeyEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GCEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<TardisControlEntity>> TARDIS_CONTROL_ENTITY_TYPE =
            ENTITY_TYPES.register("tardis_control", GCEntityTypeBuilders::tardisControl);

    public static final Supplier<EntityType<TardisKeyEntity>> TARDIS_KEY_ENTITY_TYPE =
            ENTITY_TYPES.register("tardis_key", GCEntityTypeBuilders::tardisKey);

    public static void registerEntityTypes(IEventBus eventBus) {
        Constants.LOG.info("Registered GC Entity Types (NeoForge)");
        ENTITY_TYPES.register(eventBus);
        TardisControlEntity.TYPE = TARDIS_CONTROL_ENTITY_TYPE;
        TardisKeyEntity.TYPE = TARDIS_KEY_ENTITY_TYPE;
    }
}