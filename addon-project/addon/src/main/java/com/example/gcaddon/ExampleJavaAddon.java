package com.example.gcaddon;

import com.noahtnt2009.gallifreyan_chronicles.addon.api.AddonContext;
import com.noahtnt2009.gallifreyan_chronicles.addon.api.GCAddon;

public class ExampleJavaAddon implements GCAddon {
    private AddonContext context;
    private int takeOffs;

    @Override
    public void onInitialize(AddonContext context) {
        this.context = context;
        context.logger().info("Example Java addon started. Its data folder is {}", context.dataFolder());

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
