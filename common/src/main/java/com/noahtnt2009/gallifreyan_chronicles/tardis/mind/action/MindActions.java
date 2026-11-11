package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.AddFearAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.AddStressAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.AddTrustAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.BalkAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.FightAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.HitBackAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.LockDoorsAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.MessageAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.ParticlesAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.RefuseAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.SavageAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.ShockAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.builtin.SoundAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindIds;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* every action type json can use. addons call register to add their own */
public final class MindActions {
    private static final Map<String, MapCodec<? extends MindAction>> TYPES = new ConcurrentHashMap<>();

    public static final Codec<MindAction> CODEC = Codec.STRING.partialDispatch("type",
            action -> DataResult.success(action.type()),
            id -> {
                MapCodec<? extends MindAction> codec = TYPES.get(MindIds.normalize(id));
                return codec == null
                        ? DataResult.error(() -> "unknown tardis mind action " + id)
                        : DataResult.success(codec);
            });

    public static final Codec<List<MindAction>> LIST_CODEC = CODEC.listOf();

    static {
        register(RefuseAction.TYPE, RefuseAction.CODEC);
        register(BalkAction.TYPE, BalkAction.CODEC);
        register(FightAction.TYPE, FightAction.CODEC);
        register(AddStressAction.TYPE, AddStressAction.CODEC);
        register(AddTrustAction.TYPE, AddTrustAction.CODEC);
        register(AddFearAction.TYPE, AddFearAction.CODEC);
        register(SoundAction.TYPE, SoundAction.CODEC);
        register(MessageAction.TYPE, MessageAction.CODEC);
        register(ParticlesAction.TYPE, ParticlesAction.CODEC);
        register(LockDoorsAction.TYPE, LockDoorsAction.CODEC);
        register(ShockAction.TYPE, ShockAction.CODEC);
        register(HitBackAction.TYPE, HitBackAction.CODEC);
        register(SavageAction.TYPE, SavageAction.CODEC);
    }

    private MindActions() {
    }

    public static void register(String type, MapCodec<? extends MindAction> codec) {
        TYPES.put(MindIds.normalize(type), codec);
    }

    public static void run(List<MindAction> actions, MindContext context) {
        for (MindAction action : actions) {
            action.run(context);
        }
    }
}
