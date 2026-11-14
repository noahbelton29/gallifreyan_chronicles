package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisKeyEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public class GCEntityTypes {
    public static EntityType<TardisControlEntity> TARDIS_CONTROL_ENTITY_TYPE;
    public static EntityType<TardisKeyEntity> TARDIS_KEY_ENTITY_TYPE;

    public static void registerEntityTypes() {
        TARDIS_CONTROL_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
                GCUtil.modIdentifier("tardis_control"), GCEntityTypeBuilders.tardisControl());
        TardisControlEntity.TYPE = () -> TARDIS_CONTROL_ENTITY_TYPE;

        TARDIS_KEY_ENTITY_TYPE = Registry.register(BuiltInRegistries.ENTITY_TYPE,
                GCUtil.modIdentifier("tardis_key"), GCEntityTypeBuilders.tardisKey());
        TardisKeyEntity.TYPE = () -> TARDIS_KEY_ENTITY_TYPE;

        Constants.LOG.info("Registered GC Entity Types");
    }
}
