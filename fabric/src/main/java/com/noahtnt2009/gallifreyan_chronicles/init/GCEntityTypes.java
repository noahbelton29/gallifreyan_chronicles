package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisKeyEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public class GCEntityTypes {
    public static EntityType<TardisControlEntity> TARDIS_CONTROL_ENTITY_TYPE;
    public static EntityType<TardisKeyEntity> TARDIS_KEY_ENTITY_TYPE;

    public static void registerEntityTypes() {
        TARDIS_CONTROL_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
                GCUtils.modIdentifier("tardis_control"), GCEntityTypeBuilders.tardisControl());
        TardisControlEntity.TYPE = () -> TARDIS_CONTROL_ENTITY_TYPE;

        TARDIS_KEY_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
                GCUtils.modIdentifier("tardis_key"), GCEntityTypeBuilders.tardisKey());
        TardisKeyEntity.TYPE = () -> TARDIS_KEY_ENTITY_TYPE;

        Constants.LOG.info("Registered GC Entity Types");
    }
}
