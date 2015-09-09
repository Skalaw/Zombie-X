package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.handlers.BoundedCamera;
import com.asda.zombiex.handlers.GameStateManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * @author Skala
 */
public abstract class GameState {
    protected GameStateManager gsm;
    protected Game game;

    protected SpriteBatch sb;
    protected BoundedCamera cam;
    protected OrthographicCamera hudCam;

    protected GameState(GameStateManager gsm) {
        this.gsm = gsm;
        game = gsm.getGame();
        sb = game.getSpriteBatch();
        cam = game.getCamera();
        hudCam = game.getHudCamera();
    }

    public abstract void handleInput();
    public abstract void update(float dt);
    public abstract void render();
    public abstract void dispose();
}
