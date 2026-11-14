package uk.nbforge.gallifreyan_chronicles.tardis.mind.action;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SparksAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.ShieldAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SlamDoorsAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.RevealAction;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.AddFearAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.AddStressAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.AddTrustAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.BalkAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.FightAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.HitBackAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.LashOutAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.LockDoorsAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.MessageAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.ParticlesAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.RefuseAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SavageAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.ShockAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SoundAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindIds;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.action.MindActionBuiltinCodec;

/* every action type json can use. addons call register to add their own */
public final class MindActions {
    private static final Map<String, MapCodec<? extends MindAction>> TYPES = new ConcurrentHashMap<>();

    static {
        register(RefuseAction.TYPE, MindActionBuiltinCodec.REFUSE);
        register(BalkAction.TYPE, MindActionBuiltinCodec.BALK);
        register(FightAction.TYPE, MindActionBuiltinCodec.FIGHT);
        register(AddStressAction.TYPE, MindActionBuiltinCodec.ADD_STRESS);
        register(AddTrustAction.TYPE, MindActionBuiltinCodec.ADD_TRUST);
        register(AddFearAction.TYPE, MindActionBuiltinCodec.ADD_FEAR);
        register(SoundAction.TYPE, MindActionBuiltinCodec.SOUND);
        register(MessageAction.TYPE, MindActionBuiltinCodec.MESSAGE);
        register(ParticlesAction.TYPE, MindActionBuiltinCodec.PARTICLES);
        register(LockDoorsAction.TYPE, MindActionBuiltinCodec.LOCK_DOORS);
        register(ShockAction.TYPE, MindActionBuiltinCodec.SHOCK);
        register(HitBackAction.TYPE, MindActionBuiltinCodec.HIT_BACK);
        register(SavageAction.TYPE, MindActionBuiltinCodec.SAVAGE);
        register(LashOutAction.TYPE, MindActionBuiltinCodec.LASH_OUT);
        register(RevealAction.TYPE, MindActionBuiltinCodec.REVEAL);
        register(SlamDoorsAction.TYPE, MindActionBuiltinCodec.SLAM_DOORS);
        register(ShieldAction.TYPE, MindActionBuiltinCodec.SHIELD);
        register(SparksAction.TYPE, MindActionBuiltinCodec.SPARKS);
    }

    private MindActions() {
    }

    public static @Nullable MapCodec<? extends MindAction> byType(String id) {
        return TYPES.get(MindIds.normalize(id));
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
