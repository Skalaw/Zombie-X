package com.asda.zombiex.handlers;

import com.badlogic.gdx.InputAdapter;

/**
 * @author Skala
 */
public class InputController extends InputAdapter {
    public static InputKeys[] inputKeys;

    static {
        inputKeys = new InputKeys[2];
        inputKeys[0] = new InputKeys();
        inputKeys[1] = new InputKeys();
    }

    public static void update() {
        inputKeys[0].update();
        inputKeys[1].update();
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (pointer > 1) {
            return false;
        }

        inputKeys[pointer].x = x;
        inputKeys[pointer].y = y;
        inputKeys[pointer].down = true;
        return true;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (pointer > 1) {
            return false;
        }

        inputKeys[pointer].x = x;
        inputKeys[pointer].y = y;
        inputKeys[pointer].down = true;
        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (pointer > 1) {
            return false;
        }

        inputKeys[pointer].x = x;
        inputKeys[pointer].y = y;
        inputKeys[pointer].down = false;
        return true;
    }

}
