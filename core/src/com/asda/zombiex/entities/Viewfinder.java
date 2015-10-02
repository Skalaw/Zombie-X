package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.asda.zombiex.handlers.B2DVars;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * @author Skala
 */
public class Viewfinder {
    private final static int RADIUS = 50;
    private Texture tex;
    private Body playerBody;
    private int offsetX;
    private int offsetY;
    private float offsetFromAngleX;
    private float offsetFromAngleY;

    private float radian;

    public Viewfinder(Body playerBody, int width, int height) {
        this.playerBody = playerBody;
        tex = Game.res.getTexture("viewfinder");

        offsetX = (width - tex.getWidth()) / 2;
        offsetY = (height - tex.getHeight()) / 2;
        setRadian((float) Math.PI); // init (180 angle)
    }

    public void render(SpriteBatch sb) {
        sb.begin();
        sb.draw(tex,
                playerBody.getPosition().x * B2DVars.PPM - offsetX - offsetFromAngleX,
                playerBody.getPosition().y * B2DVars.PPM - offsetY - offsetFromAngleY);
        sb.end();
    }

    public void setRadian(float radian) {
        this.radian = radian;
        calculateDistance();
    }

    public float getRadian() {
        return radian;
    }

    private void calculateDistance() {
        offsetFromAngleX = (float) Math.cos(radian) * RADIUS;
        offsetFromAngleY = (float) -Math.sin(radian) * RADIUS;
    }
}
