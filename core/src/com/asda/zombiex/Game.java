package com.asda.zombiex;

import com.asda.zombiex.handlers.BoundedCamera;
import com.asda.zombiex.handlers.Content;
import com.asda.zombiex.handlers.GameStateManager;
import com.asda.zombiex.handlers.InputController;
import com.asda.zombiex.handlers.InputKeys;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game extends ApplicationAdapter {
    public static int V_WIDTH = 320;
    public static int V_HEIGHT = 240;
    public static final int SCALE = 2;

    private SpriteBatch sb;
    private BoundedCamera cam;
    private OrthographicCamera hudCam;

    private GameStateManager gsm;
    public static Content res;

    @Override
    public void create() {
        V_WIDTH = (V_HEIGHT * Gdx.graphics.getWidth()) / Gdx.graphics.getHeight();

        Gdx.input.setInputProcessor(new InputController());

        res = new Content();
        res.loadTexture("images/badlogic.jpg", "badlogic");

        cam = new BoundedCamera();
        cam.setToOrtho(false, V_WIDTH, V_HEIGHT);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, V_WIDTH, V_HEIGHT);

        sb = new SpriteBatch();

        gsm = new GameStateManager(this);
    }

    @Override
    public void render() {
        gsm.update(Gdx.graphics.getDeltaTime());
        gsm.render();
        InputKeys.update();
    }

    @Override
    public void dispose() {
        res.removeAll();
    }

    public SpriteBatch getSpriteBatch() {
        return sb;
    }

    public BoundedCamera getCamera() {
        return cam;
    }

    public OrthographicCamera getHudCamera() {
        return hudCam;
    }
}
