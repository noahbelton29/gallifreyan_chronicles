package com.noahtnt2009.gallifreyan_chronicles.block.entity;

public interface IDoorController {

    DoorState getState();

    void setState(DoorState state);

    boolean isAnimating();

    void interact(boolean sneaking);

    void tick(); // optional for animation timing

}