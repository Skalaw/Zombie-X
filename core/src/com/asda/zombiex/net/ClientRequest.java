package com.asda.zombiex.net;

/**
 * @author Skala
 */
public class ClientRequest {

    public static String getMoving(float intensity) {
        return "moving: " + intensity + "|";
    }

    public static String getAnalogRadian(float radian) {
        return "radian: " + radian + "|";
    }

    public static String getJump() {
        return "jump|";
    }

    public static String getFire() {
        return "fire|";
    }
}
