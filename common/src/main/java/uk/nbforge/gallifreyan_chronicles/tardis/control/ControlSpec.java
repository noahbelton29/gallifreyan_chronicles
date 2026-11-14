package uk.nbforge.gallifreyan_chronicles.tardis.control;


import net.minecraft.world.phys.Vec3;


public record ControlSpec(String id, Vec3 offset, float width, float height, float depth,
                          float rotX, float rotY, float rotZ) {

    public ControlSpec(String id, Vec3 offset, float width, float height) {
        this(id, offset, width, height, width, 0f, 0f, 0f);
    }

    public ControlSpec(String id, Vec3 offset, float width, float height, float depth) {
        this(id, offset, width, height, depth, 0f, 0f, 0f);
    }
}