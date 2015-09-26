package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * @author Skala
 */
public class Player extends B2DSprite {

    public Player(Body body) {
        super(body);

        Texture tex = Game.res.getTexture("jenkins");
        TextureRegion sprite = new TextureRegion(tex, 0, 0, 25, 50);

        setAnimation(sprite, 1 / 12f);
    }

    /**
     * Set moving left or right
     * @param intensity in percentage
     */

    public void moving(float intensity) {
        float impulse = intensity * 0.4f;

        Vector2 pos = body.getPosition();
        body.applyLinearImpulse(impulse, 0, pos.x, pos.y, true);
    }

    /**
     * Release slowly when idle control horizontally
     */

    public void braking() {
        Vector2 vel = body.getLinearVelocity();
        body.setLinearVelocity(vel.x * 0.9f, vel.y);
    }

    /**
     * Jump player
     */

    public void jump() {
        Vector2 vel = body.getLinearVelocity();
        body.setLinearVelocity(vel.x, 0);
        body.applyForceToCenter(0, 175, true);
    }
}
