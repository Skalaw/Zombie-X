package com.asda.zombiex.desktop;

import com.asda.zombiex.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = Game.V_WIDTH * Game.SCALE;
        config.height = Game.V_HEIGHT * Game.SCALE;
        new LwjglApplication(new Game(), config);
    }
}
