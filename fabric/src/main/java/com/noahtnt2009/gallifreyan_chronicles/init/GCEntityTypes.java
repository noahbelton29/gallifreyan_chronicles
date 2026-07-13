package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisKeyEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class GCEntityTypes {
    public static EntityType<TardisControlEntity> TARDIS_CONTROL_ENTITY_TYPE;
    public static EntityType<TardisKeyEntity> TARDIS_KEY_ENTITY_TYPE;

    public static void registerEntityTypes() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, GCUtils.of("tardis_control"));

        TARDIS_CONTROL_ENTITY_TYPE = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                GCUtils.of("tardis_control"),
                EntityType.Builder.of(TardisControlEntity::new, MobCategory.MISC)
                        .sized(TardisControlEntity.DEFAULT_WIDTH, TardisControlEntity.DEFAULT_HEIGHT)
                        .noSummon()
                        .noSave()
                        .build(key)
        );

        TardisControlEntity.TYPE = () -> TARDIS_CONTROL_ENTITY_TYPE;

        ResourceKey<EntityType<?>> keyEntityKey = ResourceKey.create(Registries.ENTITY_TYPE, GCUtils.of("tardis_key"));

        TARDIS_KEY_ENTITY_TYPE = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                GCUtils.of("tardis_key"),
                EntityType.Builder.of(TardisKeyEntity::new, MobCategory.MISC)
                        .sized(TardisKeyEntity.DEFAULT_WIDTH, TardisKeyEntity.DEFAULT_HEIGHT)
                        .noSummon()
                        .noSave()
                        .build(keyEntityKey)
        );

        TardisKeyEntity.TYPE = () -> TARDIS_KEY_ENTITY_TYPE;

        Constants.LOG.info("Registered GC Entity Types");
    }
}