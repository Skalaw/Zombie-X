package com.asda.zombiex.handlers;

import com.badlogic.gdx.InputAdapter;

/**
 * @author Skala
 */
public class InputController extends InputAdapter { // TODO: need second touch

    @Override
    public boolean mouseMoved(int x, int y) {
        InputKeys.x = x;
        InputKeys.y = y;
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        InputKeys.x = x;
        InputKeys.y = y;
        InputKeys.down = true;
        return true;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        InputKeys.x = x;
        InputKeys.y = y;
        InputKeys.down = true;
        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        InputKeys.x = x;
        InputKeys.y = y;
        InputKeys.down = false;
        return true;
    }

}
