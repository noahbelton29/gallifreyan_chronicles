package com.noahtnt2009.gallifreyan_chronicles.client.tardis;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
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
            String name = exterior.getOwnerName();
            if (name != null) data.putString(KEY, name);
        }
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtils.modIdentifier("tardis_owner");
    }
}
