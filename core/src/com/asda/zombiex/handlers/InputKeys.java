package com.asda.zombiex.handlers;

/**
 * @author Skala
 */
public class InputKeys {
    public int x;
    public int y;
    public boolean down;
    public boolean pdown;

    public void update() {
        pdown = down;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isPressed() {
        return down && !pdown;
    }

    public boolean isReleased() {
        return !down && pdown;
    }
}
