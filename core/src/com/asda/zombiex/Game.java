package com.asda.zombiex;

import com.asda.zombiex.handlers.BoundedCamera;
import com.asda.zombiex.handlers.Content;
import com.asda.zombiex.handlers.GameStateManager;
import com.asda.zombiex.handlers.InputController;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game extends ApplicationAdapter {
    public static int V_WIDTH = 480;
    public static int V_HEIGHT = 360;
    public static final int SCALE = 2;
    public static final float STEP = 1 / 60f;

    private SpriteBatch sb;
    private BoundedCamera cam;
    private OrthographicCamera hudCam;

    private GameStateManager gsm;
    public static Content res;

    private String ipHost;

    public Game() {
        this("0.0.0.0");
    }

    public Game(String ipHost) {
        this.ipHost = ipHost;
    }

    @Override
    public void create() {
        V_WIDTH = (V_HEIGHT * Gdx.graphics.getWidth()) / Gdx.graphics.getHeight();

        Gdx.input.setInputProcessor(new InputController());

        initResources();

        cam = new BoundedCamera();
        cam.setToOrtho(false, V_WIDTH, V_HEIGHT);
        hudCam = new OrthographicCamera();
        hudCam.setToOrtho(false, V_WIDTH, V_HEIGHT);

        sb = new SpriteBatch();

        gsm = new GameStateManager(this);
    }

    private void initResources() {
        res = new Content();
        // buttons menu
        res.loadTexture("images/button_single.png", "button_single");
        res.loadTexture("images/button_server.png", "button_server");
        res.loadTexture("images/button_client.png", "button_client");

        // game
        res.loadTexture("images/jenkins.png", "jenkins");
        res.loadTexture("images/bullet.png", "bullet");
        res.loadTexture("images/viewfinder.png", "viewfinder");

        // controllers
        res.loadTexture("images/controllers/analog.png", "analog");
        res.loadTexture("images/controllers/button.png", "button");
        res.loadTexture("images/controllers/fire.png", "fire");
    }

    @Override
    public void render() {
        gsm.update(Gdx.graphics.getDeltaTime());
        gsm.render();
        InputController.update();
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

    public String getIpHost() {
        return ipHost;
    }
}
