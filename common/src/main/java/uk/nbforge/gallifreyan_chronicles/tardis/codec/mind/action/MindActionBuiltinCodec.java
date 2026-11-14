package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
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
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.RevealAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SavageAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.ShieldAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.ShockAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SlamDoorsAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SoundAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.builtin.SparksAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums;

public final class MindActionBuiltinCodec {
    public static final MapCodec<AddFearAction> ADD_FEAR = Codec.FLOAT.fieldOf("amount")
            .xmap(AddFearAction::new, AddFearAction::amount);

    public static final MapCodec<AddStressAction> ADD_STRESS = Codec.FLOAT.fieldOf("amount")
            .xmap(AddStressAction::new, AddStressAction::amount);

    public static final MapCodec<AddTrustAction> ADD_TRUST = Codec.FLOAT.fieldOf("amount")
            .xmap(AddTrustAction::new, AddTrustAction::amount);

    public static final MapCodec<BalkAction> BALK = MapCodec.unit(new BalkAction());

    public static final MapCodec<FightAction> FIGHT = Codec.FLOAT.optionalFieldOf("stress", 2f)
            .xmap(FightAction::new, FightAction::stress);

    public static final MapCodec<HitBackAction> HIT_BACK = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("max_damage", 6f).forGetter(HitBackAction::maxDamage),
            Codec.FLOAT.optionalFieldOf("min_damage", 2f).forGetter(HitBackAction::minDamage),
            Codec.FLOAT.optionalFieldOf("push", 0.9f).forGetter(HitBackAction::push)
    ).apply(inst, HitBackAction::new));

    public static final MapCodec<LashOutAction> LASH_OUT = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("from_severity", 0.35f).forGetter(LashOutAction::fromSeverity),
            Codec.FLOAT.optionalFieldOf("min_damage", 1f).forGetter(LashOutAction::minDamage),
            Codec.FLOAT.optionalFieldOf("max_damage", 4f).forGetter(LashOutAction::maxDamage),
            Codec.FLOAT.optionalFieldOf("push", 0.7f).forGetter(LashOutAction::push),
            Codec.FLOAT.optionalFieldOf("lift", 0.3f).forGetter(LashOutAction::lift)
    ).apply(inst, LashOutAction::new));

    public static final MapCodec<LockDoorsAction> LOCK_DOORS = Codec.BOOL.optionalFieldOf("only_outside", false)
            .xmap(LockDoorsAction::new, LockDoorsAction::onlyOutside);

    public static final MapCodec<MessageAction> MESSAGE = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("key").forGetter(MessageAction::key),
            MindEnums.codec(ChatFormatting.class).optionalFieldOf("colour", ChatFormatting.GRAY).forGetter(MessageAction::colour)
    ).apply(inst, MessageAction::new));

    private static final Codec<SimpleParticleType> PARTICLES_SIMPLE_PARTICLE = BuiltInRegistries.PARTICLE_TYPE.byNameCodec()
            .comapFlatMap(type -> type instanceof SimpleParticleType simple
                    ? DataResult.success(simple)
                    : DataResult.error(() -> "particle needs extra options, use a simple one"), simple -> simple);

    public static final MapCodec<ParticlesAction> PARTICLES = RecordCodecBuilder.mapCodec(inst -> inst.group(
            PARTICLES_SIMPLE_PARTICLE.fieldOf("particle").forGetter(ParticlesAction::particle),
            Codec.INT.optionalFieldOf("count", 10).forGetter(ParticlesAction::count),
            Vec3.CODEC.optionalFieldOf("spread", new Vec3(0.5, 0.5, 0.5)).forGetter(ParticlesAction::spread),
            Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(ParticlesAction::speed),
            Codec.FLOAT.optionalFieldOf("height", 0f).forGetter(ParticlesAction::height)
    ).apply(inst, ParticlesAction::new));

    public static final MapCodec<RefuseAction> REFUSE = MapCodec.unit(new RefuseAction());

    public static final MapCodec<RevealAction> REVEAL = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.BOOL.optionalFieldOf("personality", true).forGetter(RevealAction::personality),
            Codec.BOOL.optionalFieldOf("if_it_reacted", false).forGetter(RevealAction::ifItReacted),
            MindEnums.codec(Reveal.class).optionalFieldOf("traits").forGetter(RevealAction::traits)
    ).apply(inst, RevealAction::new));

    public static final MapCodec<SavageAction> SAVAGE = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("max_damage", 10f).forGetter(SavageAction::maxDamage),
            Codec.FLOAT.optionalFieldOf("min_damage", 4f).forGetter(SavageAction::minDamage),
            Codec.INT.optionalFieldOf("burn_seconds", 3).forGetter(SavageAction::burnSeconds)
    ).apply(inst, SavageAction::new));

    public static final MapCodec<ShieldAction> SHIELD = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("radius", 2.5f).forGetter(ShieldAction::radius),
            Codec.FLOAT.optionalFieldOf("push", 0.6f).forGetter(ShieldAction::push),
            Codec.FLOAT.optionalFieldOf("lift", 0.2f).forGetter(ShieldAction::lift)
    ).apply(inst, ShieldAction::new));

    public static final MapCodec<ShockAction> SHOCK = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("range", 4f).forGetter(ShockAction::range),
            Codec.FLOAT.optionalFieldOf("damage", 2f).forGetter(ShockAction::damage),
            Codec.FLOAT.optionalFieldOf("push", 0.6f).forGetter(ShockAction::push),
            Codec.FLOAT.optionalFieldOf("lift", 0.3f).forGetter(ShockAction::lift)
    ).apply(inst, ShockAction::new));

    public static final MapCodec<SlamDoorsAction> SLAM_DOORS = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("volume", 1.5f).forGetter(SlamDoorsAction::volume),
            Codec.FLOAT.optionalFieldOf("shake", 0.6f).forGetter(SlamDoorsAction::shake),
            Codec.BOOL.optionalFieldOf("lock", false).forGetter(SlamDoorsAction::lock)
    ).apply(inst, SlamDoorsAction::new));

    public static final MapCodec<SoundAction> SOUND = RecordCodecBuilder.mapCodec(inst -> inst.group(
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound").forGetter(SoundAction::sound),
            Codec.FLOAT.optionalFieldOf("volume", 1f).forGetter(SoundAction::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1f).forGetter(SoundAction::pitch)
    ).apply(inst, SoundAction::new));

    public static final MapCodec<SparksAction> SPARKS = RecordCodecBuilder.mapCodec(inst -> inst.group(
            MindEnums.codec(SparksAction.Size.class).optionalFieldOf("size", SparksAction.Size.SMALL).forGetter(SparksAction::size),
            Codec.DOUBLE.optionalFieldOf("height", 1.0).forGetter(SparksAction::height)
    ).apply(inst, SparksAction::new));

    private MindActionBuiltinCodec() {
    }
}
