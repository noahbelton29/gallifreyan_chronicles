package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentHolder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

public class TardisConsoleBlockEntity extends BlockEntity implements GeoBlockEntity, ComponentHolder {
    public static Supplier<BlockEntityType<TardisConsoleBlockEntity>> TYPE;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ComponentStore components = new ComponentStore();

    public Entity asEntity() {
        return Entity.of(this, this::sync);
    }

    public void sync() {
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public TardisConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    public TardisConsole getConsole() {
        return ConsoleSystem.consoleOf(asEntity());
    }

    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public ComponentStore componentStore() {
        return components;
    }
}
