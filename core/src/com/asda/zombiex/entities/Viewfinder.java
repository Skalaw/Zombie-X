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

    private float angle;

    public Viewfinder(Body playerBody, int width, int height) {
        this.playerBody = playerBody;
        tex = Game.res.getTexture("viewfinder");

        offsetX = (width - tex.getWidth()) / 2;
        offsetY = (height - tex.getHeight()) / 2;
    }

    public void render(SpriteBatch sb) {
        sb.begin();
        sb.draw(tex,
                playerBody.getPosition().x * B2DVars.PPM - offsetX - offsetFromAngleX,
                playerBody.getPosition().y * B2DVars.PPM - offsetY - offsetFromAngleY);
        sb.end();
    }

    public void setAngle(float angle) {
        this.angle = angle;
        calculateDistance();
    }

    private void calculateDistance() {
        float radian = angle * (float) Math.PI / 180;

        offsetFromAngleX = (float) Math.sin(radian) * RADIUS;
        offsetFromAngleY = (float) Math.cos(radian) * RADIUS;
    }
}
