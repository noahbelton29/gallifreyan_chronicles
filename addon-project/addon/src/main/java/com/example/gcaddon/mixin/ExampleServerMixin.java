package com.example.gcaddon.mixin;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class ExampleServerMixin {
    @Inject(method = "stopServer", at = @At("HEAD"))
    private void example_java_addon$onStop(CallbackInfo ci) {
        LoggerFactory.getLogger("example_java_addon").info("The server is stopping (logged by a mixin)");
    }
}
