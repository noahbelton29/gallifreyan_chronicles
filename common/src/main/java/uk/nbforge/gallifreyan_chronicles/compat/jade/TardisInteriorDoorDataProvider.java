package uk.nbforge.gallifreyan_chronicles.compat.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class TardisInteriorDoorDataProvider implements IServerDataProvider<BlockAccessor> {
    public static final TardisInteriorDoorDataProvider INSTANCE = new TardisInteriorDoorDataProvider();
    public static final String KEY = "DoorLocked";
    public static final String ROLE_KEY = "DoorRole";
    public static final String STYLE_KEY = "DoorStyle";

    @Override
    public void appendServerData(@NonNull CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof InteriorDoorBlockEntity door) {
            data.putString(ROLE_KEY, door.role().name());
            data.putString(STYLE_KEY, door.getRoundelBlock());
            if (door.followsTardisLock()) data.putBoolean(KEY, door.isTardisLocked());
        }
    }

    @Override
    public @NonNull Identifier getUid() {
        return GCUtil.modIdentifier("interior_door_lock_data");
    }
}
