package uk.nbforge.gallifreyan_chronicles.compat.jade;

import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class TardisOwnerDataProvider implements IServerDataProvider<BlockAccessor> {
    public static final TardisOwnerDataProvider INSTANCE = new TardisOwnerDataProvider();
    public static final String KEY = "TardisOwner";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof TardisExteriorBlockEntity exterior) {
            TardisApi.of(exterior).flatMap(Tardis::ownerName).ifPresent(name -> data.putString(KEY, name));
        }
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtil.modIdentifier("tardis_owner");
    }
}
