package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.handlers.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

/**
 * @author Skala
 */
public class Menu extends GameState {
    private Texture img;

    public Menu(GameStateManager gsm) {
        super(gsm);

        img = Game.res.getTexture("badlogic");

        cam.setToOrtho(false, Game.V_WIDTH, Game.V_HEIGHT);
    }

    @Override
    public void handleInput() {

    }

    @Override
    public void update(float dt) {

    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(img, 0, 0);
        sb.end();
    }

    @Override
    public void dispose() {

    }
}
