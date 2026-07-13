package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisKeyEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GCEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<TardisControlEntity>> TARDIS_CONTROL_ENTITY_TYPE =
            ENTITY_TYPES.register("tardis_control", () ->
                    EntityType.Builder.of(TardisControlEntity::new, MobCategory.MISC)
                            .sized(TardisControlEntity.DEFAULT_WIDTH, TardisControlEntity.DEFAULT_HEIGHT)
                            .noSummon()
                            .noSave()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    GCUtils.of("tardis_control"))));

    public static final Supplier<EntityType<TardisKeyEntity>> TARDIS_KEY_ENTITY_TYPE =
            ENTITY_TYPES.register("tardis_key", () ->
                    EntityType.Builder.of(TardisKeyEntity::new, MobCategory.MISC)
                            .sized(TardisKeyEntity.DEFAULT_WIDTH, TardisKeyEntity.DEFAULT_HEIGHT)
                            .noSummon()
                            .noSave()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                    GCUtils.of("tardis_key"))));

    public static void registerEntityTypes(IEventBus eventBus) {
        Constants.LOG.info("Registered GC Entity Types (NeoForge)");
        ENTITY_TYPES.register(eventBus);
        TardisControlEntity.TYPE = TARDIS_CONTROL_ENTITY_TYPE;
        TardisKeyEntity.TYPE = TARDIS_KEY_ENTITY_TYPE;
    }
}