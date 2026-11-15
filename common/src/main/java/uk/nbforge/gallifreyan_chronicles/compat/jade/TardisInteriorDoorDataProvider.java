package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class TardisInteriorDoorDataProvider implements IServerDataProvider<BlockAccessor> {
    public static final TardisInteriorDoorDataProvider INSTANCE = new TardisInteriorDoorDataProvider();
    public static final String KEY = "DoorLocked";

    @Override
    public void appendServerData(@NonNull CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof InteriorDoorBlockEntity door && door.followsTardisLock()) {
            data.putBoolean(KEY, door.isTardisLocked());
        }
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtil.modIdentifier("interior_door_lock_data");
    }
}
