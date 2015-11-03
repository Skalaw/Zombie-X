package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.handlers.GameStateManager;
import com.asda.zombiex.handlers.InputController;
import com.asda.zombiex.handlers.InputKeys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector3;

/**
 * @author Skala
 */
public class Menu extends GameState {
    private Texture buttonSingle; // TODO: Create class for buttons
    private Texture buttonServer;
    private Texture buttonClient;

    private float buttonX;
    private float textIpY;
    private float buttonSingleY;
    private float buttonServerY;
    private float buttonClientY;

    private Vector3 vec;
    private BitmapFont font;
    private String connectIp = "";

    public Menu(GameStateManager gsm) {
        super(gsm);

        buttonSingle = Game.res.getTexture("button_single");
        buttonServer = Game.res.getTexture("button_server");
        buttonClient = Game.res.getTexture("button_client");

        buttonX = (Game.V_WIDTH - buttonSingle.getWidth()) / 2f;
        textIpY = Game.V_HEIGHT;
        buttonSingleY = Game.V_HEIGHT * 0.75f;
        buttonServerY = buttonSingleY - buttonSingle.getHeight() * 1.5f;
        buttonClientY = buttonServerY - buttonServer.getHeight() * 1.5f;

        cam.setToOrtho(false, Game.V_WIDTH, Game.V_HEIGHT);

        vec = new Vector3();
        font = new BitmapFont();
        font.setColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void handleInput() {
        InputKeys inputKeys = InputController.inputKeys[0];

        vec.set(inputKeys.x, inputKeys.y, 0);
        cam.unproject(vec);

        if (inputKeys.isPressed()) {
            if (vec.x > 0 && vec.x < Game.V_WIDTH / 4) {
                if (vec.y > textIpY - 30 && vec.y < textIpY) {
                    showPopupIp();
                }
            }

            if (vec.x > buttonX && vec.x < buttonX + buttonSingle.getWidth()) {
                if (vec.y > buttonSingleY && vec.y < buttonSingleY + buttonSingle.getHeight()) {
                    gsm.setState(GameStateManager.PLAY);
                    gsm.setSinglePlayer();
                }
            }

            if (vec.x > buttonX && vec.x < buttonX + buttonServer.getWidth()) {
                if (vec.y > buttonServerY && vec.y < buttonServerY + buttonServer.getHeight()) {
                    gsm.setState(GameStateManager.PLAY);
                    gsm.setServer(game.getIpHost());
                }
            }

            if (vec.x > buttonX && vec.x < buttonX + buttonClient.getWidth()) {
                if (vec.y > buttonClientY && vec.y < buttonClientY + buttonClient.getHeight()) {
                    gsm.setState(GameStateManager.PLAY);
                    gsm.setClient(connectIp);
                }
            }
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
    }

    @Override
    public void render() {
        sb.setProjectionMatrix(cam.combined);

        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();

        String text = "Host ip: " + game.getIpHost() + " client: " + connectIp;

        font.draw(sb, text, 0, textIpY);
        sb.draw(buttonSingle, buttonX, buttonSingleY);
        sb.draw(buttonServer, buttonX, buttonServerY);
        sb.draw(buttonClient, buttonX, buttonClientY);
        sb.end();
    }

    @Override
    public void dispose() {

    }

    private void showPopupIp() {
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                connectIp = text;
            }

            @Override
            public void canceled() {

            }
        }, "Please enter Ip", "", "Ip address");
    }
}
