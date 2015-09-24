package com.asda.zombiex.handlers;

import com.badlogic.gdx.Gdx;

/**
 * @author Skala
 */
public class InputKeys {
    private static final float SIZE_EDGE_FIELD_SCREEN_TOUCH = 0.15f;

    public static int x;
    public static int y;
    public static boolean down;
    public static boolean pdown;

    public static void update() {
        pdown = down;
    }

    public static boolean isDown() {
        return down;
    }

    public static boolean isPressed() {
        return down && !pdown;
    }

    public static boolean isReleased() {
        return !down && pdown;
    }
}
