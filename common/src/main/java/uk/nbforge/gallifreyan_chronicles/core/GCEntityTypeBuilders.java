package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisKeyEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class GCEntityTypeBuilders {
    private GCEntityTypeBuilders() {
    }

    public static EntityType<TardisControlEntity> tardisControl() {
        return EntityType.Builder.of(TardisControlEntity::new, MobCategory.MISC)
                .sized(TardisControlEntity.DEFAULT_WIDTH, TardisControlEntity.DEFAULT_HEIGHT)
                .noSummon()
                .noSave()
                .build(key("tardis_control"));
    }

    public static EntityType<TardisKeyEntity> tardisKey() {
        return EntityType.Builder.of(TardisKeyEntity::new, MobCategory.MISC)
                .sized(TardisKeyEntity.DEFAULT_WIDTH, TardisKeyEntity.DEFAULT_HEIGHT)
                .noSummon()
                .noSave()
                .build(key("tardis_key"));
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, GCUtil.modIdentifier(name));
    }
}
