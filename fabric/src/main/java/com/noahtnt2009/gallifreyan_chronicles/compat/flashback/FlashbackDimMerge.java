package com.noahtnt2009.gallifreyan_chronicles.compat.flashback;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import com.moulberry.flashback.Flashback;
import qouteall.q_misc_util.MiscNetworking;

/* in a replay the client gets two dimension lists from immersive portals: the
   recording's one (with the tardis worlds) and the replay server's own (only
   overworld, nether, end). the second one replaced the first, so the client
   forgot the tardis worlds and the door showed nothing. in a replay we keep
   every dimension we've been told about. this runs before the list is used, so
   a tardis world never gets removed and made again (that happened when a flashback
   export started, and the portal stopped drawing a bit later) */
final class FlashbackDimMerge {
    private static CompoundTag ids, types, seaLevels;
    private static boolean merging;

    private FlashbackDimMerge() {
    }

    static boolean merge(Object packetObj) {
        if (merging) return false;
        MiscNetworking.DimIdSyncPacket packet = (MiscNetworking.DimIdSyncPacket) packetObj;
        /* isInReplay is false for a moment while flashback jumps on the timeline,
           which wiped the saved list right when it was needed. only forget it once
           the replay server is gone */
        if (Flashback.getReplayServer() == null) {
            ids = null;
            return false;
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
        if (added == 0) return false;
        merging = true;
        try {
            new MiscNetworking.DimIdSyncPacket(newIds, newTypes, newSea).handle();
        } finally {
            merging = false;
        }
        return true;
    }
}
