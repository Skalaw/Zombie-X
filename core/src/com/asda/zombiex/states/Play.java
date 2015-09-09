package com.asda.zombiex.states;

import com.asda.zombiex.handlers.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
 * @author Skala
 */
public class Play extends GameState {

    public Play(GameStateManager gsm) {
        super(gsm);
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 1, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void dispose() {

    }
}
