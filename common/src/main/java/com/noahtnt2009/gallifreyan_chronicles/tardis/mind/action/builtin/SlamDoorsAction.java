package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisDrumShakePayload;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindContext;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

/* {"type": "slam_doors", "volume": 1.5, "shake": 0.6, "lock": true}
   it slams its doors shut with a bang and the room shakes. lock: locks them too */
public record SlamDoorsAction(float volume, float shake, boolean lock) implements MindAction {
    public static final String TYPE = Constants.MOD_ID + ":slam_doors";
    public static final MapCodec<SlamDoorsAction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("volume", 1.5f).forGetter(SlamDoorsAction::volume),
            Codec.FLOAT.optionalFieldOf("shake", 0.6f).forGetter(SlamDoorsAction::shake),
            Codec.BOOL.optionalFieldOf("lock", false).forGetter(SlamDoorsAction::lock)
    ).apply(inst, SlamDoorsAction::new));

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void run(MindContext context) {
        if (lock) LockDoorsAction.closeAndLock(context.tardis);
        else context.tardis.closeDoors();
        context.everywhere((level, pos) -> {
            level.playSound(null, BlockPos.containing(pos), GCSounds.TARDIS_DOOR_CLOSE, SoundSource.BLOCKS, volume, 0.6F);
            TardisDrumShakePayload.broadcast(level, pos, shake);
        });
    }
}
