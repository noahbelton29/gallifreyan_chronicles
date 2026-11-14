package com.example.gcaddon;

import uk.nbforge.gallifreyan_chronicles.addon.api.AddonContext;
import uk.nbforge.gallifreyan_chronicles.addon.api.GCAddon;
import net.minecraft.world.item.Item;

public class ExampleJavaAddon implements GCAddon {
    private AddonContext context;
    private int takeOffs;

    @Override
    public void onInitialize(AddonContext context) {
        this.context = context;

        context.registerItem("example_gem", Item::new);

        context.onTakeOff(tardis -> {
            takeOffs++;
            context.logger().info("A TARDIS is taking off (that is takeoff number {})", takeOffs);
        });
        context.onLand(tardis -> context.logger().info("A TARDIS has landed"));
    }

    @Override
    public void onShutdown() {
        context.logger().info("Example Java addon shutting down after {} takeoff(s)", takeOffs);
    }
}
