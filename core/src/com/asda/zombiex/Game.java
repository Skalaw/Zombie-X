package com.asda.zombiex;

import com.asda.zombiex.handlers.Content;
import com.asda.zombiex.handlers.InputController;
import com.asda.zombiex.handlers.InputKeys;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game extends ApplicationAdapter {
	public static int V_WIDTH = 320;
	public static int V_HEIGHT = 240;
	public static final int SCALE = 2;

	private SpriteBatch sb;
	private Texture img;

	public static Content res;
	
	@Override
	public void create () {
		V_WIDTH = (V_HEIGHT * Gdx.graphics.getWidth()) / Gdx.graphics.getHeight();

		Gdx.input.setInputProcessor(new InputController());

		res = new Content();
		res.loadTexture("images/badlogic.jpg", "badlogic");

        sb = new SpriteBatch();
		img = res.getTexture("badlogic");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        sb.begin();
        sb.draw(img, 0, 0);
        sb.end();
        InputKeys.update();
	}

	@Override
	public void dispose() {
		res.removeAll();
	}

    public SpriteBatch getSpriteBatch() {
        return sb;
    }
}
