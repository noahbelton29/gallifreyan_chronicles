package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import qouteall.q_misc_util.MiscNetworking;

/* in a replay the client gets two dimension lists from immersive portals: the
   recording's one (with the tardis worlds) and the replay server's own (only
   overworld, nether, end). the second one replaced the first, so the client
   forgot the tardis worlds and the door showed nothing. in a replay we keep
   every dimension we've been told about */
final class FlashbackDimMerge {
    private static CompoundTag ids, types, seaLevels;
    private static boolean merging;

    private FlashbackDimMerge() {
    }

    static void afterSync(Object packetObj) {
        if (merging) return;
        MiscNetworking.DimIdSyncPacket packet = (MiscNetworking.DimIdSyncPacket) packetObj;
        if (!FlashbackChunksImpl.inReplay()) {
            ids = null;
            return;
        }
        CompoundTag newIds = packet.dimIntIdTag().copy();
        CompoundTag newTypes = packet.dimTypeTag().copy();
        CompoundTag newSea = packet.dimSeaLevelTag().copy();
        int added = 0;
        if (ids != null) {
            Set<Integer> used = new HashSet<>();
            for (String key : newIds.keySet()) used.add(newIds.getIntOr(key, Integer.MIN_VALUE));
            for (String key : ids.keySet()) {
                if (newIds.contains(key)) continue;
                int id = ids.getIntOr(key, Integer.MIN_VALUE);
                if (id == Integer.MIN_VALUE || used.contains(id)) continue;
                newIds.putInt(key, id);
                used.add(id);
                Tag type = types.get(key);
                if (type != null) newTypes.put(key, type.copy());
                Tag sea = seaLevels.get(key);
                if (sea != null) newSea.put(key, sea.copy());
                added++;
            }
        }
        ids = newIds;
        types = newTypes;
        seaLevels = newSea;
        if (added > 0) {
            merging = true;
            try {
                new MiscNetworking.DimIdSyncPacket(newIds, newTypes, newSea).handle();
            } finally {
                merging = false;
            }
        }
    }
}
